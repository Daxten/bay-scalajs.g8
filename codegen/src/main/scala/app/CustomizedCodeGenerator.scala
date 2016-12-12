package app

import slick.codegen.SourceCodeGenerator
import slick.sql.SqlProfile.ColumnOption
import slick.{model => m}

class CustomizedCodeGenerator(val model: m.Model) extends SourceCodeGenerator(model) {
  override val ddlEnabled = false

  override def entityName =
    (dbName: String) => dbName.split("_").map(_.capitalize).mkString

  override def tableName = (dbName: String) => entityName(dbName) + "Table"

  override def Table = new Table(_) { table =>

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
      override def doc: String = ""

      override def code = {
        val args = columns
          .map(
            c =>
              c.default
                .map(v => s"${c.name}: ${c.exposedType} = $v")
                .getOrElse(
                  s"${c.name}: ${c.exposedType}"
              ))
          .mkString(", ")

        val prns = parents.map(" with " + _).mkString("")

        s"""
           |case class $name($args) $prns
             """.stripMargin
      }
    }

    override def Column = new Column(_) { column =>
      override def rawType: String = model.tpe match {
        case "java.sql.Date" => "org.threeten.bp.LocalDate"
        case "java.sql.Time" => "org.threeten.bp.LocalTime"
        case "java.sql.Timestamp" =>
          model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map {
            case "timestamptz" => "org.threeten.bp.OffsetDateTime"
            case _             => "org.threeten.bp.LocalDateTime"
          } getOrElse "org.threeten.bp.LocalDateTime"
        case "String" =>
          model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map {
            case "json" | "jsonb" => "io.circe.Json"
            case "hstore"   => "Map[String, String]"
            case "_text"    => "List[String]"
            case "_varchar" => "List[String]"
            case "geometry" => "com.vividsolutions.jts.geom.Geometry"
            case "int8[]"   => "List[Long]"
            case "interval" => "org.threeten.bp.Duration"
            case e          => "String"
          } getOrElse "String"
        case _ => super.rawType.asInstanceOf[String]
      }

      override def code =
        s"""val $name: Rep[$actualType] = column[$actualType]("${model.name}"${options.filter(_ => !rawType.startsWith("List")).map(", " + _).mkString("")})"""

      override def rawName = entityName(model.name).uncapitalize
    }
  }

  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
    s"""
       |package $pkg
       |
       |import bay.driver.CustomizedPgDriver
       |import shared.models.Shared$container._
       |
       |// AUTO-GENERATED Slick data model
       |
       |trait $container${parentType.map(t => s" extends $t").getOrElse("")} {
       |
       |  val profile = CustomizedPgDriver
       |  import profile.api._
       |
       |  ${indent(code.replace("$CONTAINER", container))}
       |
       |}
     """.stripMargin
  }
}
