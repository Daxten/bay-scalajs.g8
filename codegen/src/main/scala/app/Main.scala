package app

import java.io.{File, PrintWriter}

import bay.driver.CustomizedPgDriver
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

object Main extends App {

  import scala.collection.JavaConversions._
  import scala.concurrent.ExecutionContext.Implicits.global


  val configFile = new File("server/conf/application.conf")
  println("Using Configuration File: " + configFile.getAbsolutePath)
  val config = ConfigFactory.parseFile(configFile)

  for {
    dbConfig <- config.getObject("slick.dbs")
  } yield {
    val short = dbConfig._1
    val name = short.capitalize

    val url = config.getString(s"slick.dbs.$short.db.url")
    val driver = config.getString(s"slick.dbs.$short.db.driver")
    val user = config.getString(s"slick.dbs.$short.db.user")
    val password = config.getString(s"slick.dbs.$short.db.password")

    val excluded = List("schema_version")

    val profile = CustomizedPgDriver
    val db = CustomizedPgDriver.api.Database.forURL(url, driver = "org.postgresql.Driver", user = user, password = password)

    def sourceGen = db.run(profile.createModel(
      Option(profile.defaultTables.map(ts => ts.filterNot(t => excluded contains t.name.name))
      ))) map { model =>
      new CustomizedCodeGenerator(model)
    }

    Await.ready(sourceGen.map {
      case codegen =>
        codegen.writeToFile("bay.driver.CustomizedPgDriver", "server/app", "models.auto_generated", name, s"$name.scala")
    } recover {
      case e: Throwable => e.printStackTrace()
    }, Duration.Inf)

    val createdFile = new File(s"server/app/models/auto_generated/$name.scala")
    val modelSource = Source.fromFile(createdFile).mkString
    val sharedSource = modelSource
      .split("\n")
      .map(_.trim)
      .filter(_.startsWith("case class"))
      .map(_
        .replace("java.time.OffsetDateTime", "Long")
        .replace("java.time.LocalDateTime", "Long")
        .replace("java.time.Duration", "Long")
        .replace("{", "")
      ).mkString("\n  ")

    val sharedTemplate =
      s"""
         |package shared.models.auto_generated
         |
         |object Shared$name {
         |  $sharedSource
         |}
     """.stripMargin

    val sharedObjectTemplate =
      s"""
         |package shared.models
         |
       |object Shared$name extends shared.models.auto_generated.Shared$name {
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

    new PrintWriter(s"shared/src/main/scala/shared/models/auto_generated/Shared$name.scala") {
      write(sharedTemplate)
      close()
    }

  }
}

