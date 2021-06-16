import play.sbt.PlayImport._
import sbt.Tests.{Group, SubProcess}
import sbt._

private object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.play" %% "play-json" % "2.6.14",
    "com.typesafe.play" %% "play-json-joda" % "2.6.14",
    "uk.gov.hmrc" %% "govuk-template" % "5.61.0-play-27",
    "uk.gov.hmrc" %% "play-ui" % "8.21.0-play-27",
    "uk.gov.hmrc" %% "http-caching-client" % "9.2.0-play-27",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "5.1.0",
    "uk.gov.hmrc" %% "play-language" % "5.1.0-play-27",
    "uk.gov.hmrc" %% "play-partials" % "8.1.0-play-27"
  )

  object Test {
    val scope: String = "test"

    def apply(): Seq[ModuleID] =
      Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "org.mockito" % "mockito-core" % "3.7.0" % scope,
        "org.scalacheck" %% "scalacheck" % "1.15.2" % scope
      )
  }

  def apply(): Seq[ModuleID] = compile ++ Test()

}
