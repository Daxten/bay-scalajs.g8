package app

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}
import java.sql.DriverManager

import bay.driver.CustomizedPgDriver
import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway
import org.scalafmt.Scalafmt

import scala.meta._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.meta.Defn.Trait

object Main extends App {
  import scala.collection.JavaConversions._
  import scala.concurrent.ExecutionContext.Implicits.global

  val configFile = new File("server/conf/application.conf")
  println("Using Configuration File: " + configFile.getAbsolutePath)
  val config = ConfigFactory.parseFile(configFile).resolve()

  println("Starting codegen")
  for {
    dbConfig <- config.getObject("slick.dbs")
  } yield {
    val short = dbConfig._1
    val name  = short.capitalize

    val url      = config.getString(s"slick.dbs.$short.db.url")
    val driver   = config.getString(s"slick.dbs.$short.db.driver")
    val user     = config.getString(s"slick.dbs.$short.db.user")
    val password = config.getString(s"slick.dbs.$short.db.password")

    val excluded = List("schema_version") ++ config.getStringList(s"slick.dbs.$short.db.exclude")

    val profile = CustomizedPgDriver
    val db      = CustomizedPgDriver.api.Database.forURL(url, driver = driver, user = user, password = password)

    if (args.contains("recreate")) {
      println("Removing Database to rerun all migrations")
      val c         = DriverManager.getConnection(url.reverse.dropWhile(_ != '/').reverse, user, password)
      val statement = c.createStatement()
      try {
        statement.executeUpdate(s"DROP DATABASE ${url.reverse.takeWhile(_ != '/').reverse};")
      } catch {
        case scala.util.control.NonFatal(e) =>
      } finally {
        statement.close()
        c.close()
      }
    }

    println("Creating Database if necessary")
    val c         = DriverManager.getConnection(url.reverse.dropWhile(_ != '/').reverse, user, password)
    val statement = c.createStatement()
    try {
      statement.executeUpdate(s"CREATE DATABASE ${url.reverse.takeWhile(_ != '/').reverse};")
    } catch {
      case scala.util.control.NonFatal(e) =>
    } finally {
      statement.close()
      c.close()
    }

    println("Migrating using flyway..")
    val flyway = new Flyway
    flyway.setDataSource(url, user, password)
    flyway.setValidateOnMigrate(false) // Creates problems with windows machines
    flyway.setLocations(s"filesystem:server/conf/db/migrations/$short")
    flyway.migrate()

    println("Starting codegen..")
    def sourceGen =
      db.run(profile.createModel(Option(profile.defaultTables.map(ts => ts.filterNot(t => excluded contains t.name.name))))) map { model =>
        new CustomizedCodeGenerator(model)
      }

    Await.ready(
      sourceGen.map(codegen =>
        codegen.writeToFile("bay.driver.CustomizedPgDriver", "dbdriver/src/main/scala", "models.auto_generated.slick", name, s"$name.scala")) recover {
        case e: Throwable => e.printStackTrace()
      },
      Duration.Inf
    )

    val createdFile = new File(s"dbdriver/src/main/scala/models/auto_generated/slick/$name.scala")
    createdFile.getParentFile.mkdirs
    val modelSource       = scala.io.Source.fromFile(createdFile).mkString
    val sharedCaseClasses = modelSource.split("\n").map(_.trim).filter(_.startsWith("case class"))
    val sharedSource      = sharedCaseClasses.mkString("\n  ")

    val filteredSource =
      modelSource.split("\n").filterNot(_.trim.startsWith("case class")).mkString("\n")

    val sharedTemplate = s"""
                            |package shared.models.auto_generated
                            |
                            |trait Shared$name {
                            |  $sharedSource
                            |}
     """.stripMargin

    val sharedObjectTemplate = s"""
                                  |package shared.models
                                  |
                                  |object Shared$name extends shared.models.auto_generated.Shared$name {
                                  |   // You can do changes in this file, even though it will get autocreated if it's missing, it won't be overwritten
                                  |
                                  |}
     """.stripMargin

    val objectFile = new File(s"shared/src/main/scala/shared/models/Shared$name.scala")
    if (objectFile.createNewFile()) {
      new PrintWriter(objectFile) {
        write(sharedObjectTemplate)
        close()
      }
    }

    val style = org.scalafmt.config.Config.fromHocon(scala.io.Source.fromFile(".scalafmt.conf").mkString).right.get
    def formatCode(code: String) = Scalafmt.format(code, style).get

    {
      // Shared Model
      val path = s"shared/src/main/scala/shared/models/auto_generated/Shared$name.scala"
      val file = new File(path)
      val sourceText = if (file.exists()) {
        val source = file.parse[Source].get

        val newSource = sharedCaseClasses.foldLeft[Tree](source)((s, caseClass) => {
          val caseClassStat = caseClass.parse[Stat].get
          val (newCaseClassName, newCaseClassParamss) = caseClassStat.collect {
            case q"case class $tname (...$paramss)" =>
              (tname.value, paramss)
          }.head

          s.transform {
            case c @ q"case class $tname (...$paramss)" if tname.value == newCaseClassName =>
              if (newCaseClassParamss != paramss) {
                println(s"- Updating CaseClass $newCaseClassName")
                caseClassStat
              } else {
                println(s"- No Changes necessary for $newCaseClassName")
                c
              }
            case c @ q"case class $tname (...$paramss) { ..$body }" if tname.value == newCaseClassName =>
              if (newCaseClassParamss != paramss) {
                println(s"- Updating CaseClass $newCaseClassName (preserving body)")
                caseClassStat.transform {
                  case q"case class $tname (...$paramss)" =>
                    q"case class $tname (...$paramss) { ..$body }"
                }
              } else {
                println(s"No Changes necessary for $newCaseClassName")
                c
              }
            case q"trait $tname { ..$body }" if s.collect {
                  case q"case class $cname (...$paramss)" if cname.value == newCaseClassName             => 1
                  case q"case class $cname (...$paramss) { ..$body }" if cname.value == newCaseClassName => 1
                }.isEmpty =>
              println(s"- Can't find $newCaseClassName, adding it to trait $tname")
              q"trait $tname { ..${body :+ caseClassStat} }"
          }
        })

        newSource.syntax
      } else {
        sharedTemplate
      }

      Files.write(Paths.get(path), formatCode(sourceText).getBytes)
    }

    new PrintWriter(createdFile) {
      write(filteredSource)
      close()
    }
  }
}
