object Dependencies {

  // Server (jvm)
  val slickPg     = "0.15.1" // https://github.com/tminglei/slick-pg
  val slick       = "3.2.+" // http://slick.lightbend.com/
  val playSlick   = "3.0.+" // https://github.com/playframework/play-slick
  val playCirce   = "2.6-0.8.0" // https://github.com/jilen/play-circe
  val flywayPlay  = "4.0.+" // https://github.com/flyway/flyway-play
  val bcrypt      = "3.+" // https://github.com/t3hnar/scala-bcrypt
  val betterFiles = "3.0.+" // https://github.com/pathikrit/better-files
  val macwire     = "2.3.+" // https://github.com/adamw/macwire

  // ScalaJs
  val scalajsReact = "1.0.1" // https://github.com/japgolly/scalajs-react/blob/master/doc/USAGE.md
  val scalajsDom   = "0.9.0" // https://scala-js.github.io/scala-js-dom/

  // jsDependencies
  val react = "15.5.4" // https://facebook.github.io/react/

  // Shared Dependencies
  val autowire      = "0.3.1" // https://github.com/lihaoyi/autowire
  val upickle       = "0.4.4" // http://www.lihaoyi.com/upickle-pprint/upickle/
  val cats          = "0.9.0" // https://github.com/typelevel/cats
  val scalaJavaTime = "2.0.0-M12" // https://github.com/cquiroz/scala-java-time
  val circeVersion  = "0.8.0" // https://circe.github.io/circe

  // Codegen only
  val flyway        = "4.0.3" // https://flywaydb.org/documentation/api/
  val scalaMeta     = "1.8.0" // https://github.com/scalameta/scalameta
  val scalaFmt      = "0.6.8" // https://github.com/olafurpg/scalafmt
  val swaggerParser = "1.0.28" // https://github.com/swagger-api/swagger-parser
}
