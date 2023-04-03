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
      "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2"
    )
  )
