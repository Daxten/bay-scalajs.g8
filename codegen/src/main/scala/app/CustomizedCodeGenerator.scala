package app

import slick.codegen.SourceCodeGenerator
import slick.profile.SqlProfile.ColumnOption
import slick.{model => m}

class CustomizedCodeGenerator(val model: m.Model) extends SourceCodeGenerator(model) {
  override val ddlEnabled = false

  override def entityName = (dbName: String) => dbName.split("_").map(_.capitalize).mkString

  override def tableName = (dbName: String) => entityName(dbName) + "Table"

  override def Table = new Table(_) {
    table =>

    val E = entityName(model.name.table)
    val T = tableName(model.name.table)
    val Q = TableValue.rawName

    override def TableValue = new TableValue {
      override def rawName = {
        val raw = entityName(model.name.asString).uncapitalize
        if (raw.endsWith("s")) raw else raw + "s"
      }
    }

    override def autoIncLastAsOption = true

    override def EntityType = new EntityTypeDef {
      override def code = {
        val args = columns.map(c =>
          c.default.map(v =>
            s"${c.name}: ${c.exposedType} = $v"
          ).getOrElse(
            s"${c.name}: ${c.exposedType}"
          )
        ).mkString(", ")

        val jsArgs = columns.map(c =>
          c.exposedType match {
            case "Option[java.time.OffsetDateTime]" => s"${c.name}.map(_.toEpochSecond)"
            case "Option[java.time.LocalDateTime]" => s"${c.name}.map(e => java.time.OffsetDateTime.of(e, java.time.ZoneOffset.ofHours(1)).toEpochSecond)"
            case "java.time.OffsetDateTime" => s"${c.name}.toEpochSecond"
            case "java.time.LocalDateTime" => s"java.time.OffsetDateTime.of(${c.name}, java.time.ZoneOffset.ofHours(1)).toEpochSecond"
            case "Option[java.time.Duration]" => s"${c.name}.map(_.getSeconds)"
            case "java.time.Duration" => s"${c.name}.getSeconds"
            case _ => c.name
          }
        ).mkString(", ")

        val prns = parents.map(" with " + _).mkString("")

        s"""
           |case class $name($args) $prns {
           |  def asJs = shared.models.auto_generated.Shared$$CONTAINER.$name($jsArgs)
           |}
             """.stripMargin
      }
    }

    override def Column = new Column(_) {
      column =>
      override def rawType: String = model.tpe match {
        case "java.sql.Date" => "java.time.LocalDate"
        case "java.sql.Time" => "java.time.LocalTime"
        case "java.sql.Timestamp" =>
          model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map {
            case "timestamptz" => "java.time.OffsetDateTime"
            case _ => "java.time.LocalDateTime"
          } getOrElse "java.time.LocalDateTime"
        case "String" => model.options.find(_.isInstanceOf[ColumnOption.SqlType])
          .map(_.asInstanceOf[ColumnOption.SqlType].typeName).map({ e =>
          e match {
            case "hstore" => "Map[String, String]"
            case "_text" => "List[String]"
            case "_varchar" => "List[String]"
            case "geometry" => "com.vividsolutions.jts.geom.Geometry"
            case "int8[]" => "List[Long]"
            case "interval" => "java.time.Duration"
            case e => "String"
          }
        }).getOrElse("String")
        case _ => super.rawType.asInstanceOf[String]
      }

      override def code = s"""val $name: Rep[$actualType] = column[$actualType]("${model.name}"${options.filter(_ => !rawType.startsWith("List")).map(", " + _).mkString("")})"""

      override def rawName = entityName(model.name).uncapitalize
    }
  }

  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
    s"""
       |package $pkg
       |
       |import bay.driver.CustomizedPgDriver
       |import scala.concurrent.{Future, ExecutionContext}
       |import play.api.db.slick.HasDatabaseConfig
       |import models.auto_generated._
       |
       |// AUTO-GENERATED Slick data model
       |
       |trait $container${parentType.map(t => s" extends $t").getOrElse("")} {
       |
       |  val driver = CustomizedPgDriver
       |  import driver.api._
       |
       |  ${indent(code.replace("$CONTAINER", container))}
       |
       |}
     """.stripMargin
  }
}


