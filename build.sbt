import sbt._
import sbt.Keys._
import ScoverageSbtPlugin._

lazy val root = project.in(file(".")).
  aggregate(stateParserJS, stateParserJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val stateParser = crossProject.
  crossType(CrossType.Full).
  settings(
    name := "stateParser",
    version := "0.1.1",
    scalaVersion := "2.11.7", 
    organization := "es.weso",
	libraryDependencies ++= Seq(
	  "org.scalatest" %%% "scalatest" % "3.0.0-M10" % "test"
	)
  ).
  jvmSettings(
	libraryDependencies ++= Seq(
		"org.scala-lang.modules" %%% "scala-parser-combinators" % "1.0.4"
    ),
	licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0"))
  ).
  jsSettings(
	libraryDependencies ++= Seq(
       "org.scala-js" %%% "scala-parser-combinators" % "1.0.2"
    ),
	licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0"))
  )
  
lazy val stateParserJVM = stateParser.jvm
lazy val stateParserJS = stateParser.js  
	 
publishMavenStyle := true

bintrayRepository in bintray := "weso-releases"

bintrayOrganization in bintray := Some("weso")

licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0"))

resolvers += "Bintray" at "http://dl.bintray.com/weso/weso-releases"

EclipseKeys.useProjectId := true

// Publish site info
site.settings

site.publishSite

site.includeScaladoc()

