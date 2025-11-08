val scala3Version = "3.7.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Scala 3 Project Template",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-deprecation", "-feature"),

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.2.1" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.1.0" % Test,
      "org.http4s" %% "http4s-ember-server" % "0.23.33",
      "org.http4s" %% "http4s-circe" % "0.23.33",
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.12.2"
    )
  )
