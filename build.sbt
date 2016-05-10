import sbt.Project.projectToRef

name in ThisBuild := """Play Template"""

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

resolvers in ThisBuild ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.sonatypeRepo("snapshots")
)

lazy val web = (project in file("web"))
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.10.4",
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.2.0"
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % "0.14.7" / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
      "org.webjars.bower" % "react" % "0.14.7" / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",
      "org.webjars.bower" % "moment" % "2.13.0" / "moment-with-locales.js" minified "moment-with-locales.min.js" commonJSName "moment",
      "org.webjars.bower" % "moment-timezone" % "0.5.3" / "moment-timezone-with-data.js" minified "moment-timezone-with-data.min.js" dependsOn "moment-with-locales.js"
    )
  )
  .dependsOn(sharedJS)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)

lazy val jsProjects = Seq(web)

lazy val driver = (project in file("driver"))
  .settings(
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.github.tminglei" %% "slick-pg" % "0.12.2",
      "com.github.tminglei" %% "slick-pg_date2" % "0.12.2"
    )
  )

lazy val codegen = (project in file("codegen"))
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick-codegen" % "3.1.1"
  ))
  .dependsOn(driver)

lazy val server = (project in file("server"))
  .settings(
    libraryDependencies ++= Seq(
      jdbc,
      cache,
      ws,
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "com.typesafe.play" %% "play-slick" % "1.1.1",
      "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
      "jp.t2v" %% "play2-auth" % "0.14.1",
      "org.flywaydb" %% "flyway-play" % "2.2.1"
    ),
    scalaJSProjects := jsProjects,
    pipelineStages := Seq(scalaJSProd),
    routesGenerator := InjectedRoutesGenerator
  )
  .dependsOn(driver, sharedJVM)
  .aggregate(jsProjects.map(projectToRef): _*)
  .enablePlugins(PlayScala, DockerPlugin, JavaServerAppPackaging)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "upickle" % "0.3.6",
    "com.lihaoyi" %%% "scalarx" % "0.3.1",
    "me.chrons" %%% "diode-data" % "0.5.1",
    "org.scalaz" %%% "scalaz-core" % "7.2.2"
  ))
  .jsConfigure(_ enablePlugins ScalaJSPlay)
  .jsSettings(
  )

lazy val sharedJVM = shared.jvm

lazy val sharedJS = shared.js

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value