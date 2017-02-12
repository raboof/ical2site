scalacOptions := Seq("-feature", "-deprecation")

scalaVersion := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

//libraryDependencies += "ical4j" % "ical4j" % "0.9.20"
libraryDependencies += "net.sf.biweekly" % "biweekly" % "0.5.0"
libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.0"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.1"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "nu.validator" % "validator" % "16.6.29" % "test"
