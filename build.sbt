organization := "com.kostassoid"

name := "materialist"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-optimise",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

javacOptions ++= Seq(
  "-source", "1.7",
  "-target", "1.7",
  "-encoding", "UTF-8",
  "-Xlint:unchecked",
  "-Xlint:deprecation"
)

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "0.8.2.2" exclude("org.slf4j", "slf4j-log4j12"),
  "org.mongodb" % "mongo-java-driver" % "3.1.0",
  "com.typesafe" % "config" % "1.2.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)
    