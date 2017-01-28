package app

import io.swagger.parser.SwaggerParser
import io.swagger.models.Swagger
import utils.Implicits._
import scala.collection.JavaConversions._
import better.files._
import java.io.{File => JFile}

import utils.{CaseClassMetaHelper, ScalaFmtHelper}

import scala.meta._

object SwaggerCodegen extends App {
  val swaggerDir = file"server/conf/swagger"
  val swaggers   = swaggerDir.list.filter(_.extension.contains(".swagger")).map(e => (e, new SwaggerParser().read(e.pathAsString)))

  swaggers.foreach(e => createForConfig(e._1, e._2))

  def createForConfig(f: File, swagger: Swagger): Unit = {
    val apiVersion = "v" + swagger.getInfo.getVersion.replace(".", "_")
    println(s"Codegeneration for Swaggerdoc at [${f.pathAsString}] $apiVersion")

    /*
    Create Models
     */
    val modelsFolder = file"shared/src/main/scala/shared/models/swagger/$apiVersion"
    val swaggerTypeMap = Map(
      "string"   -> "String",
      "integer"  -> "Int",
      "number"   -> "Double",
      "float"    -> "Double",
      "double"   -> "Double",
      "boolean"  -> "Boolean",
      "date"     -> "LocalDate",
      "time"     -> "LocalTime",
      "dateTime" -> "OffsetDateTime"
    )

    swagger.getDefinitions.toVector.foreach {
      case (name, model) =>
        val modelName = name.toUpperCamelCase

        val propertiesAsScala: Vector[String] = model.getProperties.toVector.map { e =>
          if (e._2.getRequired) {
            s"${e._1.toCamelCase}: ${swaggerTypeMap(e._2.getType)}"
          } else {
            s"${e._1.toCamelCase}: Option[${swaggerTypeMap(e._2.getType)}]"
          }
        }

        val modelAsCaseClass = s"case class $modelName(${propertiesAsScala.mkString(", ")})"

        val targetFile = modelsFolder./(s"$modelName.scala")
        if (targetFile.notExists) {
          val template =
            s"""
            |package shared.models.swagger.$apiVersion
            |
            |$modelAsCaseClass
          """.trim.stripMargin

          targetFile.createIfNotExists(createParents = true).overwrite(template)
        } else {
          val source = targetFile.toJava.parse[Source].get
          val caseClassStat =
            modelAsCaseClass.parse[Stat].get
          val tree = CaseClassMetaHelper.updateOrInsert(source, caseClassStat)
          targetFile.write(ScalaFmtHelper.formatCode(tree.syntax))
        }
    }

  }
}
