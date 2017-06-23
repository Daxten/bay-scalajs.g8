package app.swagger

import app.SwaggerCodegen.property2Scala
import better.files.File
import io.swagger.models.Swagger
import utils.CaseClassMetaHelper
import utils.ScalaFmtHelper
import scala.meta._
import scala.meta.Source
import scala.meta.Stat
import better.files._
import scala.collection.JavaConversions._
import app._

object ModelsGen {
  def gen(swagger: Swagger, apiVersion: String, f: File): Unit = {
    println(s"- Starting Models Generator for ${f.pathAsString}")
    val modelsFolder = file"shared/src/main/scala/shared/models/swagger/${f.nameWithoutExtension}/$apiVersion"

    swagger.getDefinitions.toVector.foreach {
      case (name, model) =>
        val modelName = name.toUpperCamelCase

        val propertiesAsScala: Vector[String] = model.getProperties.toVector.map { e =>
          s"${e._1.toCamelCase}: ${property2Scala(e._2)}"
        }

        val modelAsCaseClass = s"case class $modelName(${propertiesAsScala.mkString(", ")})"

        val targetFile = modelsFolder./(s"$modelName.scala")
        if (targetFile.notExists) {
          // Create Template
          val template =
            s"""
               |package shared.models.swagger.${f.nameWithoutExtension}.$apiVersion
               |
            |import java.time._
               |
            |$modelAsCaseClass
          """.trim.stripMargin

          targetFile.createIfNotExists(createParents = true).overwrite(template)
        } else {
          // Update existing Source
          val source = targetFile.toJava.parse[Source].get
          val caseClassStat =
            modelAsCaseClass.parse[Stat].get
          val tree = CaseClassMetaHelper.updateOrInsert(source, caseClassStat)
          targetFile.write(ScalaFmtHelper.formatCode(tree.syntax))
        }
    }
  }

}
