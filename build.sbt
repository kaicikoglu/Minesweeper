val scala3Version = "3.3.3"

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := scala3Version,
  libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.18",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  libraryDependencies += ("org.scala-lang.modules" %% "scala-swing" % "3.0.0")
    .cross(CrossVersion.for3Use2_13),
  libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.14.0",
  libraryDependencies += "com.google.inject" % "guice" % "7.0.0",
  libraryDependencies += ("net.codingwell" %% "scala-guice" % "7.0.0")
    .cross(CrossVersion.for3Use2_13),
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.5")
    .cross(CrossVersion.for3Use2_13),
  libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.0",
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.0",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % "10.5.3",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
    "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
    "com.typesafe.akka" %% "akka-stream" % "2.8.5",
    "com.typesafe.slick" %% "slick" % "3.5.0",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
    "com.zaxxer" % "HikariCP" % "5.1.0",
    "org.slf4j" % "slf4j-nop" % "2.0.13",
    "com.h2database" % "h2" % "2.2.224" % Test,
    "org.postgresql" % "postgresql" % "42.2.23",
    "com.typesafe.slick" %% "slick-codegen" % "3.5.0", // Updated version
    "com.typesafe" % "config" % "1.4.1",
  ),
  jacocoReportSettings := JacocoReportSettings(
    "Jacoco Coverage Report",
    None,
    JacocoThresholds(),
    Seq(JacocoReportFormats.ScalaHTML, JacocoReportFormats.XML),
    "utf-8"
  ),
  jacocoExcludes := Seq(
    "*aview.*",
    "*MinesweeperModule*",
    "*Minesweeper*",
    "*Coordinates*",
    "*Observer*"
  ),
  javaOptions ++= Seq(
    "-Xms512M",
    "-Xmx2G"
  )
)

Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "resources"

assembly / assemblyJarName := "Minesweeper-assembly-0.1.0-SNAPSHOT.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}

lazy val model = project
  .in(file("model"))
  .settings(
    name := "model",
    commonSettings
  )
  .enablePlugins(JacocoCoverallsPlugin)

lazy val persistence = project
  .in(file("persistence"))
  .settings(
    name := "persistence",
    commonSettings
  )
  .dependsOn(model)
  .enablePlugins(JacocoCoverallsPlugin)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "core",
    commonSettings
  )
  .dependsOn(model, persistence)
  .enablePlugins(JacocoCoverallsPlugin)

lazy val tui = project
  .in(file("tui"))
  .settings(
    name := "tui",
    commonSettings
  )
  .dependsOn(model, core)
  .enablePlugins(JacocoCoverallsPlugin)

lazy val gui = project
  .in(file("gui"))
  .settings(
    name := "gui",
    commonSettings
  )
  .dependsOn(model, core)
  .enablePlugins(JacocoCoverallsPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "Minesweeper",
    commonSettings
  )
  .enablePlugins(JacocoCoverallsPlugin)
  .aggregate(model, persistence, core, tui, gui)
