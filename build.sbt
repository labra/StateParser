import sbt._
import sbt.Keys._
import ScoverageSbtPlugin._

lazy val root = project.in(file("."))

organization := "es.weso"

name := "stateParser"

scalaVersion := "2.11.7"

version := "0.0.8"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"  
)

libraryDependencies ++= Seq(
  	   "org.scalatest" %%% "scalatest" % "3.0.0-M10" % "test"
)


autoCompilerPlugins := true

// Publishing settings to BinTray

publishMavenStyle := true

bintrayRepository in bintray := "weso-releases"

bintrayOrganization in bintray := Some("weso")

licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0"))

resolvers += "Bintray" at "http://dl.bintray.com/weso/weso-releases"

// Publish site info
site.settings

site.publishSite

site.includeScaladoc()

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageMinimum := 50,
  ScoverageKeys.coverageFailOnMinimum := false
)

