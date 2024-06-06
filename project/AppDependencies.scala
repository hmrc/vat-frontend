import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val playVersion                 =  "-play-30"
  private val bootstrapPlayVersion        =  "8.6.0"
  private val scalaTestVersion            =  "3.2.18"
  private val scalatestPlusPlayVersion    =  "7.0.1"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.play" %% "play-json" % "2.10.5",
    "com.typesafe.play" %% "play-json-joda" % "2.10.5",
    "uk.gov.hmrc" %% s"http-caching-client$playVersion" % "11.2.0",
    "uk.gov.hmrc" %% s"bootstrap-frontend$playVersion" % "8.6.0",
    "uk.gov.hmrc" %% s"play-frontend-hmrc$playVersion" % "9.11.0",
    "uk.gov.hmrc" %% s"play-language$playVersion" % "8.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % Test,
    "org.jsoup" % "jsoup" % "1.17.2" % Test,
    "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % Test,
    "org.scalacheck" %% "scalacheck" % "1.18.0" % Test,
    "uk.gov.hmrc" %% s"bootstrap-test$playVersion" % bootstrapPlayVersion % Test
  )


  def apply(): Seq[sbt.ModuleID] = compile ++ test

}
