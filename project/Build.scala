import sbt._
import sbt.Keys._
import bintray.Plugin.bintraySettings
import bintray.Keys._
import Def.ScopedKey
import scala.scalajs.sbtplugin.ScalaJSPlugin._

object Build extends sbt.Build{

 // as snapshots are still not supported by bintray-sbt
 // I mark lib as release
 val isRelease = true 

 def repo = if(isRelease) "scalax-releases" else "scalax-snapshots"

 val stateParserVer = "0.0.1"

 publishMavenStyle := false

 val currentVersion = stateParserVer

 val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

 protected val bintrayPublishIvyStyle = settingKey[Boolean]("=== !publishMavenStyle") //workaround for sbt-bintray bug

 lazy val publishSettings = Seq(
    repository in bintray := this.repo,

    bintrayOrganization in bintray := Some("weso"),

    licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0")),

    bintrayPublishIvyStyle := true
  )



  /**
   * For parts of the project that we will not publish
   */
  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

  val sharedSettings = Seq(
      organization := "es.weso",
      name := "stateParser",
      scalaVersion := "2.10.4"
    )

  val scalajsResolver: URLRepository = Resolver.url("scala-js-releases",
    url("http://dl.bintray.com/content/scala-js/scala-js-releases"))(
      Resolver.ivyStylePatterns)

}
