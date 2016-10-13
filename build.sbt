scalacOptions := Seq("-feature", "-deprecation", "-Ywarn-unused-import")

scalaVersion := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

//libraryDependencies += "ical4j" % "ical4j" % "0.9.20"
libraryDependencies += "net.sf.biweekly" % "biweekly" % "0.5.0"
libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
