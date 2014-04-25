import sbt._
import sbt.Keys._
import bintray.Plugin.bintraySettings
import bintray.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._

lazy val root = project.in(file("."))//.settings(crossScalaVersions := Seq("2.10.4", "2.11.0"))

Build.sharedSettings

version := Build.currentVersion

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.1.3" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.1.0-RC2"
)

testFrameworks += new TestFramework("utest.runner.JvmFramework")

addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.2")

autoCompilerPlugins := true

bintraySettings

Build.publishSettings
