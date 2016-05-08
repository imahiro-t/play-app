name := """notify"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
//  jdbc,
  cache,
  ws,
  evolutions,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.h2database" % "h2" % "1.3.176",
  "com.typesafe.play" %% "play-mailer" % "5.0.0-M1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false