import play.sbt.PlayImport._
import sbt._

private object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.play" %% "play-json" % "2.9.4",
    "com.typesafe.play" %% "play-json-joda" % "2.9.4",
    "uk.gov.hmrc" %% "http-caching-client" % "10.0.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.13.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "6.3.0-play-28",
    "uk.gov.hmrc" %% "play-language" % "6.1.0-play-28",
    "uk.gov.hmrc" %% "url-builder" % "3.8.0-play-28"
  )

  object Test {
    val scope: String = "test"

    def apply(): Seq[ModuleID] =
      Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
        "org.jsoup" % "jsoup" % "1.15.3" % scope,
        "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
        "org.scalacheck" %% "scalacheck" % "1.17.0" % scope,
        "uk.gov.hmrc" %% "bootstrap-test-play-28" % "7.13.0" % scope

      )
  }

  def apply(): Seq[ModuleID] = compile ++ Test()

}
