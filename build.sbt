name := "monix-intro"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "io.monix"                   %% "monix"          % "2.3.3"
libraryDependencies += "org.scalatest"              %% "scalatest"      % "3.0.5"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.0"
libraryDependencies += "ch.qos.logback"             % "logback-classic" % "1.2.3"

mainClass in (Compile, run) := Some("Main")
