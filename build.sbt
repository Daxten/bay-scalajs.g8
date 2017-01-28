import sbt.Project.projectToRef
import Dependencies._

name in ThisBuild := """swagger-dev"""

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

resolvers in ThisBuild ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.url("sbt-plugins", url("http://dl.bintray.com/zalando/sbt-plugins"))(Resolver.ivyStylePatterns)
)

lazy val web = (project in file("web"))
  .settings(
    persistLauncher         := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core"            % scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "extra"           % scalajsReact,
      "org.scala-js"                      %%% "scalajs-dom"     % scalajsDom,
      "io.github.cquiroz"                 %%% "scala-java-time" % scalaJavaTime
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
      "org.webjars.bower" % "react" % react / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM"
    )
  )
  .dependsOn(sharedJS)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)

lazy val dbdriver = (project in file("dbdriver"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick"  %% "slick"               % slick,
      "com.github.tminglei" %% "slick-pg"            % slickPg,
      "com.github.tminglei" %% "slick-pg_circe-json" % slickPg
    )
  )
  .dependsOn(sharedJVM)

lazy val codegen = (project in file("codegen"))
  .settings(libraryDependencies ++= Seq(
    "org.flywaydb"         % "flyway-core"    % flyway,
    "com.typesafe.slick"   %% "slick-codegen" % slick,
    "org.scalameta"        %% "scalameta"     % scalaMeta,
    "com.geirsson"         %% "scalafmt"      % scalaFmt,
    "com.github.pathikrit" %% "better-files"  % betterFiles,
    "io.swagger"           % "swagger-parser" % "1.0.25"
  ))
  .dependsOn(dbdriver)

lazy val server = (project in file("server"))
  .settings(
    commands ++= Seq(CodegenCmd, RecreateCmd, SwaggerCmd),
    libraryDependencies ++= Seq(
      jdbc,
      cache,
      ws,
      "com.github.t3hnar"    %% "scala-bcrypt"         % bcrypt,
      "com.typesafe.slick"   %% "slick"                % slick,
      "com.typesafe.play"    %% "play-slick"           % playSlick,
      "com.vmunier"          %% "play-scalajs-scripts" % playScalajs,
      "jp.t2v"               %% "play2-auth"           % playAuth,
      "org.flywaydb"         %% "flyway-play"          % flywayPlay,
      "com.github.pathikrit" %% "better-files"         % betterFiles
    ),
    scalaJSProjects          := Seq(web),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    routesGenerator          := InjectedRoutesGenerator
  )
  .dependsOn(dbdriver)
  .enablePlugins(PlayScala, DockerPlugin, JavaServerAppPackaging)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "autowire"      % autowire,
    "com.lihaoyi" %%% "scalarx"       % scalarx,
    "me.chrons"   %%% "diode-data"    % diode,
    "org.scalaz"  %%% "scalaz-core"   % scalaz,
    "io.circe"    %%% "circe-core"    % circeVersion,
    "io.circe"    %%% "circe-generic" % circeVersion,
    "io.circe"    %%% "circe-parser"  % circeVersion,
    "com.lihaoyi" %%% "upickle"       % upickle
  ))
  .jsConfigure(_ enablePlugins ScalaJSPlugin)
  .jsSettings()

lazy val sharedJVM = shared.jvm

lazy val sharedJS = shared.js

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

lazy val CodegenCmd = Command.command("codegen") { state =>
  "codegen/run-main app.DbCodegen" ::
    state
}

lazy val SwaggerCmd = Command.command("swagger") { state =>
  "codegen/run-main app.SwaggerCodegen" ::
    state
}

lazy val RecreateCmd = Command.command("codegen-re") { state =>
  "codegen/run-main app.DbCodegen recreate" ::
    state
}
