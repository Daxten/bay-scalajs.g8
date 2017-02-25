package app

import io.swagger.parser.SwaggerParser
import io.swagger.models.Swagger

import scala.collection.JavaConversions._
import better.files._
import java.io.{File => JFile}
import java.time.OffsetDateTime

import app.swagger.ModelsGen
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import utils.CaseClassMetaHelper
import utils.ScalaFmtHelper

import scala.meta._
import scala.util.Try

object SwaggerCodegen extends App {
  val swaggerDir = file"server/conf/swagger"
  val swaggers   = swaggerDir.listRecursively.filter(_.extension.contains(".yaml")).map(e => (e, new SwaggerParser().read(e.pathAsString)))

  def property2Scala(p: Property, nested: Boolean = false): String = {
    val tpe = (p.getType, Option(p.getFormat)) match {
      case _ if p.isInstanceOf[RefProperty] =>
        p.asInstanceOf[RefProperty].getSimpleRef
      case ("integer", Some("int64"))    => "Long"
      case ("integer", _)                => "Int"
      case ("number", _)                 => "Double"
      case ("string", None)              => "String"
      case ("string", Some("byte"))      => "String"
      case ("string", Some("binary"))    => "String"
      case ("boolean", _)                => "Boolean"
      case ("string", Some("date"))      => "LocalDate"
      case ("string", Some("date-time")) => "OffsetDateTime"
      case ("array", _)                  => s"List[${property2Scala(p.asInstanceOf[ArrayProperty].getItems, nested = true)}]"
      case _                             => "String"
    }

    if (p.getRequired || nested) tpe
    else s"Option[$tpe]"
  }

  swaggers.foreach(e => createForConfig(e._1, e._2))

  def createForConfig(f: File, swagger: Swagger): Unit = {
    val apiVersion = "v" + swagger.getInfo.getVersion.replace(".", "_")
    println(s"# Codegeneration for Swaggerdoc at [${f.pathAsString}] $apiVersion")

    /*
    Create Models
     */
    ModelsGen.gen(swagger, apiVersion, f)

    /*
      Create Api
     */
    val basePath = swagger.getBasePath
    val target   = file"server/app/controllers/swagger/$apiVersion/${f.nameWithoutExtension.toUpperCamelCase.takeWhile(_ != '_')}.scala"

    case class RouterCase(routerCase: String, abstractfunc: String)

    val routerCases: Vector[RouterCase] = swagger.getPaths.toVector.flatMap {
      case (strPath, path) =>
        println(s"- Creating Router for $strPath")

        val playPath = strPath
          .split('/')
          .map { e =>
            if (e.startsWith("{")) {
              val name = e.drop(1).dropRight(1)
              if (path.getOperations
                    .flatMap(e => Option(e.getParameters))
                    .flatten
                    .filter(_.getName == name)
                    .find(e => {
                      Option(e.getIn).map(_.toLowerCase).contains("path")
                    })
                    .flatMap(_.getType)
                    .contains("integer")) {
                s"$${int($name)}"
              } else {
                "$" + e
              }
            } else e
          }
          .mkString("", "/", "")

        path.getOperationMap.toVector.map {
          case (method, op) =>
            val methodName = Option(op.getOperationId)
              .getOrElse(method.toString.toLowerCase + strPath.split('/').filterNot(_.startsWith("{")).map(_.toUpperCamelCase).mkString)

            val queryParameter = op.getParameters.toVector
              .filter(_.getIn.toLowerCase == "query")
              .map { e =>
                if (e.getRequired) {
                  s"""q"${e.getName}=$$${e.getName}""""
                } else {
                  s"""q_o"${e.getName}=$$${e.getName}""""
                }
              }

            val queryParameterStr =
              if (queryParameter.isEmpty) ""
              else {
                s" ? ${queryParameter.mkString(" ? ")}"
              }

            sealed trait RequestBodyType
            object NoBody extends RequestBodyType
            object JsonBody extends RequestBodyType
            object MultipartBody extends RequestBodyType
            object FileBody extends RequestBodyType

            val body2parser = Map[RequestBodyType, String](
              NoBody        -> "",
              JsonBody      -> "(circe.json)",
              MultipartBody -> "(parse.multipartFormData)",
              FileBody      -> "(parse.temporaryFile)"
            ).withDefault(_ => "")

            val body2content = Map[RequestBodyType, String](
              NoBody        -> "AnyContent",
              JsonBody      -> "Json",
              MultipartBody -> "MultipartFormData[Files.TemporaryFile]",
              FileBody      -> "Files.TemporaryFile"
            ).withDefault(_ => "AnyContent")

            // atm only support either json or multipart/form-data
            val bodyType: RequestBodyType = {
              if (Seq("POST", "PUT").contains(method.toString)) {
                Option(op.getConsumes).flatMap(_.headOption).map(_.toLowerCase) match {
                  case Some("multipart/form-data") =>
                    MultipartBody
                  case Some("application/json") =>
                    JsonBody
                  case _ =>
                    FileBody
                }
              } else {
                NoBody
              }
            }

            val routerCase = s"""
               |case ${method.toString}(p"$playPath"$queryParameterStr) => AsyncStack${body2parser(bodyType)} { implicit request =>
               |  constructResult($methodName(${op.getParameters.toVector
                                  .filter(e => Seq("query", "path").contains(e.getIn.toLowerCase))
                                  .map(e => s"${e.getName}")
                                  .mkString(", ")}))
               |}
             """.stripMargin

            val params = op.getParameters.toVector
              .filter(e => Seq("query", "path").contains(e.getIn.toLowerCase))
              .map { e =>
                val tpe = if (e.getType.contains("integer")) "Int" else "String"

                if (e.getRequired) {
                  s"${e.getName}: $tpe"
                } else {
                  s"${e.getName}: Option[$tpe]"
                }
              }

            val abstractFunc =
              s"""def $methodName(${params.mkString(", ")})(implicit request: RequestWithAttributes[${body2content(bodyType)}]): HttpResult[Result] """
            RouterCase(routerCase.mkString, abstractFunc)
        }
    }

    val template =
      s"""
         |package controllers.swagger.$apiVersion
         |
         |import play.api.mvc._
         |import com.google.inject.Inject
         |import play.api.routing._
         |import play.api.routing.sird._
         |import play.api.libs.circe._
         |import scala.concurrent.ExecutionContext
         |import controllers.{AuthConfigImpl, ExtendedController}
         |import jp.t2v.lab.play2.stackc.RequestWithAttributes
         |import jp.t2v.lab.play2.auth.OptionalAuthElement
         |import services.dao.UserDao
         |import io.circe.Json
         |import play.api.libs.Files
         |import io.circe.generic.auto._
         |import io.circe.syntax._
         |import shared.models.swagger.${f.nameWithoutExtension}.$apiVersion._
         |
         |trait ${f.nameWithoutExtension.toUpperCamelCase}Trait extends ExtendedController with SimpleRouter with Circe {
         |  def routes: Router.Routes = {
         |   ${routerCases.map(_.routerCase).mkString}
         |  }
         |
         |  ${routerCases.map(_.abstractfunc).mkString("\n")}
         |}
         |
         """.trim.stripMargin

    target.createIfNotExists(createParents = true).overwrite(ScalaFmtHelper.formatCode(template))
  }
}
