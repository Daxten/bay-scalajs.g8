import sbt.Project.projectToRef
import Dependencies._
import com.typesafe.sbt.packager.docker.ExecCmd

name in ThisBuild := """bay-scalajs"""

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.11"

resolvers in ThisBuild ++= Seq(Resolver.bintrayRepo("scalameta", "maven"), Resolver.jcenterRepo)

lazy val web = (project in file("web"))
  .settings(
    scalaJSUseMainModuleInitializer := true,
    enableReloadWorkflow            := false,
    emitSourceMaps                  := false,
    webpackConfigFile in fullOptJS  := Some(baseDirectory.value / "prod.webpack.config.js"),
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core"            % scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "extra"           % scalajsReact,
      "org.scala-js"                      %%% "scalajs-dom"     % scalajsDom,
      "io.github.cquiroz"                 %%% "scala-java-time" % scalaJavaTime
    ),
    npmDependencies in Compile ++= Seq(
      "react"     -> "15.5.4",
      "react-dom" -> "15.5.4"
    )
  )
  .dependsOn(sharedJS)
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalaJSWeb)

lazy val dbdriver = (project in file("dbdriver"))
  .settings(
    libraryDependencies ++= Seq(
      "com.github.tminglei" %% "slick-pg"            % slickPg,
      "com.github.tminglei" %% "slick-pg_circe-json" % slickPg
    )
  )
  .dependsOn(sharedJVM)

lazy val dbschema = (project in file("dbschema"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slick
    )
  )
  .dependsOn(sharedJVM, dbdriver)

lazy val codegen = (project in file("codegen"))
  .settings(libraryDependencies ++= Seq(
    "org.flywaydb"         % "flyway-core"    % flyway,
    "com.typesafe.slick"   %% "slick-codegen" % slick,
    "org.scalameta"        %% "scalameta"     % scalaMeta,
    "com.geirsson"         %% "scalafmt-core" % scalaFmt,
    "com.github.pathikrit" %% "better-files"  % betterFiles,
    "io.swagger"           % "swagger-parser" % swaggerParser
  ))
  .dependsOn(dbdriver)

lazy val server = (project in file("server"))
  .settings(
    name := (name in ThisBuild).value,
    commands ++= Seq(CodegenCmd, RecreateCmd, SwaggerCmd),
    WebKeys.exportedMappings in Assets := Seq(),
    libraryDependencies ++= Seq(
      filters,
      jdbc,
      cache,
      ws,
      "com.github.t3hnar"    %% "scala-bcrypt" % bcrypt,
      "com.typesafe.slick"   %% "slick"        % slick,
      "com.typesafe.play"    %% "play-slick"   % playSlick,
      "jp.t2v"               %% "play2-auth"   % playAuth,
      "org.flywaydb"         %% "flyway-play"  % flywayPlay,
      "com.github.pathikrit" %% "better-files" % betterFiles,
      "play-circe"           %% "play-circe"   % playCirce
    ),
    scalaJSProjects          := Seq(web),
    pipelineStages in Assets := Seq(scalaJSPipeline, digest, gzip),
    routesGenerator          := InjectedRoutesGenerator
  )
  .dependsOn(dbdriver, dbschema)
  .enablePlugins(PlayScala, SbtWeb, WebScalaJSBundlerPlugin, DockerPlugin, JavaServerAppPackaging)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(libraryDependencies ++= Seq(
    "de.daxten"     %%% "autowire"      % autowire,
    "org.typelevel" %%% "cats"          % cats,
    "io.circe"      %%% "circe-core"    % circeVersion,
    "io.circe"      %%% "circe-generic" % circeVersion,
    "io.circe"      %%% "circe-parser"  % circeVersion,
    "com.lihaoyi"   %%% "upickle"       % upickle
  ))
  .jsConfigure(_.enablePlugins(ScalaJSPlugin))
  .jsSettings()

lazy val sharedJVM = shared.jvm

lazy val sharedJS = shared.js

onLoad in Global := (Command
  .process("project server", _: State))
  .compose((onLoad in Global).value)

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
