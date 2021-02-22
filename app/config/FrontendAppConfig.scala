/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import java.net.URLEncoder

import com.google.inject.{Inject, Singleton}
import controllers.routes
import models.VatEnrolment
import play.api.i18n.Lang
import play.api.mvc.{Call, Request}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.language.LanguageUtils
import utils.PortalUrlBuilder

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration,
                                  environment: Environment,
                                  val servicesConfig: ServicesConfig,
                                  override val languageUtils: LanguageUtils) extends PortalUrlBuilder {

  import servicesConfig._

  private def loadConfig(key: String): String = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val customsRootUrl: String = loadConfig(s"urls.external.customs.host")

  def customsUrl(path: String): String = s"$customsRootUrl/$path"

  private lazy val contactHost: String = runModeConfiguration.getOptional[String]("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier: String = "vatfrontend"

  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val btaUrl: String = baseUrl("business-tax-account")
  lazy val vatUrl: String = baseUrl("vat")
  lazy val enrolmentStoreProxyUrl: String = baseUrl("enrolment-store-proxy")

  def payApiUrl: String = baseUrl("pay-api")

  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val loginContinueUrl: String = loadConfig("urls.loginContinue")

  val loginCallback: String = runModeConfiguration.getOptional[String](s"urls.external.login-callback").getOrElse(businessAccountHomeUrl)

  private lazy val businessAccountHost: String = runModeConfiguration.getOptional[String]("urls.business-account.host").getOrElse("")
  private lazy val helpAndContactHost: String = runModeConfiguration.getOptional[String]("urls.help-and-contact.host").getOrElse("")
  private lazy val addTaxHost: String = runModeConfiguration.getOptional[String]("urls.add-tax.host").getOrElse("")
  lazy val businessAccountHomeUrl: String = businessAccountHost + "/business-account"
  lazy val businessAccountWrongCredsUrl: String = businessAccountHost + loadConfig(s"urls.business-account.wrongCreds")
  lazy val addVatUrl: String = addTaxHost + loadConfig(s"urls.add-tax.addVat")

  lazy val businessAccountHomeAbsoluteUrl: String = getUrl("businessAccountAuthority") + "/business-account"
  lazy val btaManageAccount: String = businessAccountHost + loadConfig(s"urls.business-account.manageAccount")

  private lazy val vatSummaryHost: String = runModeConfiguration.getOptional[String]("urls.vat-summary.host").getOrElse("")

  def getVatSummaryUrl(key: String): String = s"$vatSummaryHost${runModeConfiguration.getOptional[String](s"urls.vat-summary.$key").getOrElse("")}"

  private lazy val portalHost: String = loadConfig(s"urls.external.portal.host")
  private lazy val ssoEndpoint: String = loadConfig(s"urls.external.portal.ssoUrl")
  lazy val ssoUrl: String = ssoEndpoint


  def getUrl(key: String): String = loadConfig(s"urls.$key")

  def getGovUrl(key: String): String = loadConfig(s"urls.external.govuk.$key")

  def getBusinessAccountUrl(key: String): String = businessAccountHost + loadConfig(s"urls.business-account.$key")
  def businessAccountCovidSupportUrl: String = businessAccountHost + loadConfig("urls.business-account.covidSupport")

  def getPortalUrl(key: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): String =
    buildPortalUrl(portalHost + loadConfig(s"urls.external.portal.$key"))(vatEnrolment)

  def getReturnUrl(url: String) = s"returnUrl=${URLEncoder.encode(url, "UTF-8")}"

  def getHelpAndContactUrl(key: String): String = helpAndContactHost + runModeConfiguration.getOptional[String](s"urls.help-and-contact.$key").getOrElse("")

  lazy val languageTranslationEnabled: Boolean = runModeConfiguration.getOptional[Boolean]("microservice.services.features.welsh-translation").getOrElse(true)
  lazy val vatContentOutage: Boolean = runModeConfiguration.getOptional[Boolean]("microservice.services.features.vatOutageContent").getOrElse(false)

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage(lang: String): Call = routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val emacVatEnrolmentUrl: String = loadConfig("urls.external.emac.enrol")
  lazy val emacVatActivationUrl: String = loadConfig("urls.external.emac.activate")
  lazy val emacVatLostPinUrl: String = loadConfig("urls.external.emac.lostPin")
  lazy val googleTagManagerId: String = loadConfig(s"google-tag-manager.id")

  def sessionTimeoutInSeconds: Long = 900

  def sessionCountdownInSeconds: Int = 60
}
