import sbt.Project.projectToRef
import Dependencies._

name in ThisBuild := """Play Template"""

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

resolvers in ThisBuild ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.sonatypeRepo("snapshots")
)

lazy val web = (project in file("web"))
  .settings(
    persistLauncher         := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core"        % scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "extra"       % scalajsReact,
      "org.scala-js"                      %%% "scalajs-dom" % scalajsDom
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
      "org.webjars.bower" % "react" % react / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM"
    )
  )
  .dependsOn(sharedJS)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)

lazy val driver = (project in file("driver")).settings(
  libraryDependencies ++= Seq(
    "com.github.tminglei" %% "slick-pg"          % slickPg,
    "com.github.tminglei" %% "slick-pg_threeten" % slickPg
  )
)

lazy val codegen = (project in file("codegen"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-codegen" % slick
    ))
  .dependsOn(driver)

lazy val server = (project in file("server"))
  .settings(
    commands += CodegenCmd,
    libraryDependencies ++= Seq(
      jdbc,
      cache,
      ws,
      "com.typesafe.slick" %% "slick"                % slick,
      "com.typesafe.play"  %% "play-slick"           % playSlick,
      "com.vmunier"        %% "play-scalajs-scripts" % playScalajs,
      "jp.t2v"             %% "play2-auth"           % playAuth,
      "org.flywaydb"       %% "flyway-play"          % flywayPlay
    ),
    scalaJSProjects           := Seq(web),
    pipelineStages in Assets  := Seq(scalaJSPipeline),
    routesGenerator           := InjectedRoutesGenerator,
  )
  .dependsOn(driver, sharedJVM)
  .enablePlugins(PlayScala, DockerPlugin, JavaServerAppPackaging)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"   %%% "autowire"        % autowire,
      "com.lihaoyi"   %%% "upickle"         % upickle,
      "com.lihaoyi"   %%% "scalarx"         % scalarx,
      "me.chrons"     %%% "diode-data"      % diode,
      "org.scalaz"    %%% "scalaz-core"     % scalaz,
      "io.github.soc" %%% "scala-java-time" % scalaJavaTime
    ))
  .jsConfigure(_ enablePlugins ScalaJSPlugin)
  .jsSettings()

lazy val sharedJVM = shared.jvm

lazy val sharedJS = shared.js

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

lazy val CodegenCmd = Command.command("codegen") { state =>
  "codegen/run" ::
    state
}
