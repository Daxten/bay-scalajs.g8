package app

import io.swagger.parser.SwaggerParser
import io.swagger.models.{Model, RefModel, Swagger}
import io.swagger.models.parameters._

import scala.collection.JavaConversions._
import better.files._
import java.io.{File => JFile}
import java.time.OffsetDateTime

import app.swagger.ModelsGen
import io.swagger.models.auth.In
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
    val apiVersion = "v" + swagger.getInfo.getVersion.takeWhile(_ != '.').mkString // Semantic versioning, major / minor updates should force a new class
    println(s"# Codegeneration for Swaggerdoc at [${f.pathAsString}] $apiVersion")

    /*
    Create Models
     */
    ModelsGen.gen(swagger, apiVersion, f)

    /*
      Create Api
     */
    val defaultExists = swagger.getPaths.toVector.map(_._2).flatMap(_.getOperations).exists(_.getTags.isEmpty)

    for {
      swaggerTag <- swagger.getPaths.toVector.map(_._2).flatMap(_.getOperations).flatMap(_.getTags).distinct ++ (if (defaultExists) Seq("default")
                                                                                                                 else Seq.empty)
    } {
      val packageName = f.nameWithoutExtension.toLowerCase.takeWhile(_ != '_')
      val routerName  = swaggerTag.toUpperCamelCase
      println(s"- Running Codegen for Swagger Tag $swaggerTag")
      val target = file"server/app/controllers/swagger/$apiVersion/$packageName/$routerName.scala"

      case class RouterCase(routerCase: String, abstractfunc: String)

      val routerCases: Vector[RouterCase] = swagger.getPaths.toVector
        .filter(_._2.getOperations.exists(e => e.getTags.contains(swaggerTag) || (swaggerTag == "default" && e.getTags.isEmpty)))
        .flatMap {
          case (strPath, path) =>
            println(s"-- Creating Router for $strPath")

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

            path.getOperationMap.toVector.filter(e => e._2.getTags.contains(swaggerTag) || (swaggerTag == "default" && e._2.getTags.isEmpty)).map {
              case (method, op) =>
                val methodName = Option(op.getOperationId)
                  .getOrElse(method.toString.toLowerCase + strPath.split('/').filterNot(_.startsWith("{")).map(_.toUpperCamelCase).mkString)

                println(s"--- found $method")

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

                val inClassTpe: Option[String] = op.getParameters.find(_.getIn == "body") match {
                  case None => None
                  case Some(param) =>
                    param match {
                      case e: BodyParameter =>
                        e.getSchema match {
                          case s: RefModel => Some(s.getSimpleRef)
                          case s: Model    => Some(s.getReference)
                        }
                      case e: RefParameter => Some(e.getSimpleRef)
                      case e               => None
                    }
                }

                val body2parser = Map[RequestBodyType, String](
                  NoBody        -> "",
                  JsonBody      -> inClassTpe.fold("parse.circe")(e => s"(circe.json[$e])"),
                  MultipartBody -> "(parse.multipartFormData)",
                  FileBody      -> "(parse.temporaryFile)"
                ).withDefault(_ => "")

                val body2content = Map[RequestBodyType, String](
                  NoBody        -> "AnyContent",
                  JsonBody      -> inClassTpe.fold("Json")(identity),
                  MultipartBody -> "MultipartFormData[Files.TemporaryFile]",
                  FileBody      -> "Files.TemporaryFile"
                ).withDefault(_ => "AnyContent")

                // atm only support either json or multipart/form-data
                val consumeType: RequestBodyType = {
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

                val resultType = op.getResponses.find(_._1 == "200") match {
                  case None             => "Result"
                  case Some(responseOp) => property2Scala(responseOp._2.getSchema, nested = true)
                }

                val security = (for {
                  list <- Option(op.getSecurity)
                  sec <- list.headOption
                } yield {
                  sec.toVector.flatMap(e => swagger.getSecurityDefinitions.find(_._1 == sec.head._1)).headOption
                }).flatten

                val keyDef: Option[String] = security match {
                  case Some((name, e: io.swagger.models.auth.ApiKeyAuthDefinition)) =>
                    e.getIn match {
                      case In.HEADER =>
                        Some(s"""val optApiKey = request.headers.get("${e.getName}")""")
                      case _ =>
                        None
                    }
                  case _ =>
                    None
                }

                val routerBody = keyDef match {
                  case Some(keyDefStr) =>
                    s"""
                   |$keyDefStr
                   |optApiKey match {
                   |  case None => Unauthorized.asFuture
                   |  case Some(apiKey) =>
                   |    constructResult($methodName(${op.getParameters.toVector
                         .filter(e => Seq("query", "path").contains(e.getIn.toLowerCase))
                         .map(e => s"${e.getName}")
                      .:+("apiKey")
                         .mkString(", ")})${if (resultType == "Result") "" else ".map(e => Ok(e.asJson))"})
                   |}
                    """.stripMargin.trim
                  case _ =>
                    s"""
                   |constructResult($methodName(${op.getParameters.toVector
                         .filter(e => Seq("query", "path").contains(e.getIn.toLowerCase))
                         .map(e => s"${e.getName}")
                         .mkString(", ")})${if (resultType == "Result") "" else ".map(e => Ok(e.asJson))"})
                    """.stripMargin.trim
                }

                val routerCase = s"""
               |case ${method.toString}(p"$playPath"$queryParameterStr) => Action.async${body2parser(consumeType)} { implicit request =>
               |  $routerBody
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
                  } ++ (security match {  // Add security specific parameters
                  case Some((name, e: io.swagger.models.auth.ApiKeyAuthDefinition)) =>
                    Seq("apiKey: String")
                  case _ =>
                    Seq.empty
                })

                // Functions to implement
                val abstractFunc =
                  s"""def $methodName(${params
                    .mkString(", ")})(implicit request: Request[${body2content(consumeType)}]): HttpResult[$resultType]"""
                RouterCase(routerCase.mkString, abstractFunc)
            }
        }

      val template =
        s"""
         |package controllers.swagger.$apiVersion.$packageName
         |
         |import controllers.ExtendedController
         |import io.circe.Json
         |import io.circe.generic.auto._
         |import io.circe.syntax._
         |import play.api.libs.Files
         |import play.api.libs.circe._
         |import play.api.mvc._
         |import play.api.routing._
         |import play.api.routing.sird._
         |import scalaz._
         |import Scalaz._
         |import shared.models.swagger.${f.nameWithoutExtension}.$apiVersion._
         |
         |trait $routerName extends ExtendedController with SimpleRouter with Circe {
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
}
