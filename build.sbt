val scala3Version = "3.3.3"

val gatlingExclude = Seq(
  ExclusionRule("com.typesafe.akka", "akka-actor_2.13"),
  ExclusionRule("org.scala-lang.modules", "scala-java8-compat_2.13"),
  ExclusionRule("com.typesafe.akka", "akka-slf4j_2.13")
)

val gatlingHigh = "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.11.3" % "test" excludeAll (gatlingExclude *)
val gatlingTest = "io.gatling" % "gatling-test-framework" % "3.11.3" % "test" excludeAll (gatlingExclude *)

lazy val gatlingDependencies = Seq(
  gatlingHigh,
  gatlingTest
)

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := scala3Version,
  libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.18",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  libraryDependencies += ("org.scala-lang.modules" %% "scala-swing" % "3.0.0")
    .cross(CrossVersion.for3Use2_13),
  libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.15.0",
  libraryDependencies += "com.google.inject" % "guice" % "7.0.0",
  libraryDependencies += ("net.codingwell" %% "scala-guice" % "7.0.0")
    .cross(CrossVersion.for3Use2_13),
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.5")
    .cross(CrossVersion.for3Use2_13),
  libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.1",
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.0",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % "10.5.3",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
    "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
    "com.typesafe.akka" %% "akka-stream" % "2.8.5",
    "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.5.6",
    "org.postgresql" % "postgresql" % "42.7.3",
    ("org.mongodb.scala" %% "mongo-scala-driver" % "5.1.0")
      .cross(CrossVersion.for3Use2_13)
  ),
  libraryDependencies ++= gatlingDependencies,
  libraryDependencies ++= Seq(
    ("org.apache.kafka" %% "kafka-streams-scala" % "3.7.0").cross(CrossVersion.for3Use2_13),
    "org.apache.kafka" % "kafka-clients" % "3.7.0"
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

sbtassembly.AssemblyPlugin.autoImport.assembly / assemblyJarName := "Minesweeper-assembly-0.1.0-SNAPSHOT.jar"

sbtassembly.AssemblyPlugin.autoImport.assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
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
  .enablePlugins(JacocoCoverallsPlugin, GatlingPlugin)

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
  .enablePlugins(JacocoCoverallsPlugin, GatlingPlugin)
  .aggregate(model, persistence, core, tui, gui)
