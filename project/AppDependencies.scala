import play.sbt.PlayImport._
import sbt.Tests.{Group, SubProcess}
import sbt._

private object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.play" %% "play-json" % "2.9.2",
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    "uk.gov.hmrc" %% "http-caching-client" % "9.5.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.16.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.59.0-play-28",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.71.0-play-28",
    "uk.gov.hmrc" %% "play-language" % "5.1.0-play-28"
  )

  object Test {
    val scope: String = "test"

    def apply(): Seq[ModuleID] =
      Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.14.3" % scope,
        "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
        "org.scalacheck" %% "scalacheck" % "1.15.4" % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope
      )
  }

  def apply(): Seq[ModuleID] = compile ++ Test()

}
