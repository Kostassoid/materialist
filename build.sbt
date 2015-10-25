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

packSettings

packMain := Map(
  "start" → "-Xmx256m -Dfile.encoding=UTF-8 -Dlogback.configurationFile=\"${PROG_HOME}/conf/logback.xml\" -Dconfig.path=\"${PROG_HOME}/conf/env.$ENV.conf\" com.kostassoid.materialist.MainApp"
)

packResourceDir += (baseDirectory.value / "src/main/resources" -> "conf")

packJarNameConvention := "full"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "0.8.2.2" exclude("org.slf4j", "slf4j-log4j12"),
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.0.0",
  "com.typesafe" % "config" % "1.2.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)
