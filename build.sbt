import sbt.Project.projectToRef

name in ThisBuild := """Play Template"""

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.7"

resolvers in ThisBuild += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

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
      "org.webjars.bower" % "react" % "0.14.3" / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
      "org.webjars.bower" % "react" % "0.14.3" / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM"
    )
  )
  .dependsOn(sharedJS)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)

lazy val jsProjects = Seq(web)

lazy val driver = (project in file("driver"))
  .settings(
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.github.tminglei" %% "slick-pg" % "0.10.2",
      "com.github.tminglei" %% "slick-pg_date2" % "0.10.2",
      "com.github.tminglei" %% "slick-pg_jts" % "0.10.2"
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
      "com.vividsolutions" % "jts" % "1.13",
      "com.typesafe.play" %% "play-slick" % "1.1.1",
      "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
      "jp.t2v" %% "play2-auth" % "0.14.1",
      "org.flywaydb" %% "flyway-play" % "2.2.1",
      "org.scalaz" %% "scalaz-core" % "7.2.0"
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
    "me.chrons" %%% "diode-data" % "0.5.0"
  ))
  .jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJVM = shared.jvm

lazy val sharedJS = shared.js

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value