resolvers += Resolver.jcenterRepo

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.14")

// web plugins
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.17")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")

addSbtPlugin("com.jamesward" % "play-auto-refresh" % "0.0.15")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.6.0")

addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.6.0")
