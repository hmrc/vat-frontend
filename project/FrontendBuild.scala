import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "vat-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override lazy val appOverrides: Set[ModuleID] = Set (
    "uk.gov.hmrc" %% "auth-client" % "2.32.2-play-25"
  )
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val bootstrapVersion = "4.12.0"
  private val domainVersion = "5.6.0-play-25"
  private val playHealthVersion = "3.14.0-play-25"
  private val govukTemplateVersion = "5.26.0-play-25"
  private val httpCachingClientVersion = "8.4.0-play-25"
  private val logbackJsonLoggerVersion = "4.6.0"
  private val mockitoAllVersion = "1.10.19"
  private val pegdownVersion = "1.6.0"
  private val playConditionalFormMappingVersion = "1.2.0-play-25"
  private val playLanguageVersion = "3.4.0"
  private val playPartialsVersion = "6.9.0-play-25"
  private val playUiVersion = "8.7.0-play-25"
  private val scalacheckVersion = "1.14.0"
  private val scalaTestVersion = "3.0.4"
  private val scalaTestPlusPlayVersion = "2.0.1"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.11.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoAllVersion % scope,
        "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
