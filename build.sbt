ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "top.criwits"

lazy val root = (project in file("."))
  .settings(
    name := "Learnbot",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.6",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "org.eclipse.jetty" % "jetty-server" % "11.0.14",
      "org.eclipse.jetty" % "jetty-servlet" % "11.0.14",
      "org.eclipse.jetty" % "jetty-client" % "11.0.14",
      "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided",
      "org.apache.httpcomponents" % "httpclient" % "4.5.14",
      "org.apache.httpcomponents" % "httpmime" % "4.5.14",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2",
      "org.apache.pdfbox" % "pdfbox" % "2.0.22"
    ),
    assembly / assemblyJarName := "learnbot.jar",
    assembly / mainClass := Some("top.criwits.learnbot.Main"),
    assembly / assemblyMergeStrategy := {
      case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
      case PathList("io", "grpc", xs @ _*)       => MergeStrategy.last
      case PathList(ps @ _*) if ps.last endsWith ".properties" =>
        MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".xml"   => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
      case "application.conf"                             => MergeStrategy.concat
      case "unwanted.txt"                                 => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
