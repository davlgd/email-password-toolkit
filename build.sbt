val scala3Version = "3.7.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Scala 3 Project Template",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.2.1" % Test,
      "org.http4s" %% "http4s-ember-server" % "0.23.33",
      "org.http4s" %% "http4s-dsl" % "0.23.33",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.12.2"
    )
  )
