import play.sbt.PlayImport._
import sbt.Tests.{Group, SubProcess}
import sbt._

private object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.play" %% "play-json" % "2.9.2",
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    "uk.gov.hmrc" %% "http-caching-client" % "9.6.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "6.3.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.22.0-play-28",
    "uk.gov.hmrc" %% "play-language" % "5.2.0-play-28"
  )

  object Test {
    val scope: String = "test"

    def apply(): Seq[ModuleID] =
      Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
        "org.jsoup" % "jsoup" % "1.14.3" % scope,
        "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
        "org.scalacheck" %% "scalacheck" % "1.15.4" % scope,
        "uk.gov.hmrc" %% "bootstrap-test-play-28" % "6.3.0" % scope

      )
  }

  def apply(): Seq[ModuleID] = compile ++ Test()

}
