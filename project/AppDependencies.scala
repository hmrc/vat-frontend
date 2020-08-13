import play.sbt.PlayImport._
import sbt.Tests.{Group, SubProcess}
import sbt._

private object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.play" %% "play-json" % "2.6.14",
    "com.typesafe.play" %% "play-json-joda" % "2.6.14",
    "uk.gov.hmrc" %% "govuk-template" % "5.55.0-play-27",
    "uk.gov.hmrc" %% "play-ui" % "8.11.0-play-27",
    "uk.gov.hmrc" %% "http-caching-client" % "9.1.0-play-27",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "2.21.0",
    "uk.gov.hmrc" %% "play-language" % "4.3.0-play-27",
    "uk.gov.hmrc" %% "play-partials" % "6.11.0-play-27"
  )

  object Test {
    val scope: String = "test"

    def apply(): Seq[ModuleID] =
      Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "org.mockito" % "mockito-all" % "2.0.2-beta" % scope,
        "org.scalacheck" %% "scalacheck" % "1.14.3" % scope
      )
  }

  def apply(): Seq[ModuleID] = compile ++ Test()

}
