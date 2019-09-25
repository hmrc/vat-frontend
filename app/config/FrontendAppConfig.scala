/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.mvc.Request
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig
import utils.PortalUrlBuilder

@Singleton
class FrontendAppConfig @Inject() (override val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig with PortalUrlBuilder {

  override protected def mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val customsRootUrl = loadConfig(s"urls.external.customs.host")
  def customsUrl(path: String) = s"$customsRootUrl/$path"

  private lazy val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "vatfrontend"

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val btaUrl = baseUrl("business-tax-account")
  lazy val vatUrl = baseUrl("vat")
  lazy val enrolmentStoreProxyUrl = baseUrl("enrolment-store-proxy")

  def payApiUrl: String = baseUrl("pay-api")

  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")

  val loginCallback = runModeConfiguration.getString(s"urls.external.login-callback").getOrElse(businessAccountHomeUrl)

  private lazy val businessAccountHost = runModeConfiguration.getString("urls.business-account.host").getOrElse("")
  private lazy val helpAndContactHost = runModeConfiguration.getString("urls.help-and-contact.host").getOrElse("")
  private lazy val addTaxHost = runModeConfiguration.getString("urls.add-tax.host").getOrElse("")
  lazy val businessAccountHomeUrl = businessAccountHost + "/business-account"
  lazy val businessAccountWrongCredsUrl: String = businessAccountHost + loadConfig(s"urls.business-account.wrongCreds")
  lazy val addVatUrl: String = addTaxHost + loadConfig(s"urls.add-tax.addVat")

  lazy val businessAccountHomeAbsoluteUrl: String = getUrl("businessAccountAuthority") + "/business-account"
  lazy val btaManageAccount = businessAccountHost + loadConfig(s"urls.business-account.manageAccount")

  private lazy val vatSummaryHost = runModeConfiguration.getString("urls.vat-summary.host").getOrElse("")
  def getVatSummaryUrl(key:String) = s"$vatSummaryHost${runModeConfiguration.getString(s"urls.vat-summary.$key").getOrElse("")}"

  private lazy val portalHost = loadConfig(s"urls.external.portal.host")
  private lazy val ssoEndpoint = loadConfig(s"urls.external.portal.ssoUrl")
  lazy val ssoUrl = ssoEndpoint

  def getUrl(key: String): String = loadConfig(s"urls.$key")
  def getGovUrl(key: String): String = loadConfig(s"urls.external.govuk.$key")
  def getBusinessAccountUrl(key: String): String = businessAccountHost + loadConfig(s"urls.business-account.$key")

  def getPortalUrl(key: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): String =
    buildPortalUrl(portalHost + loadConfig(s"urls.external.portal.$key"))(vatEnrolment)

  def getReturnUrl(url:String) = s"returnUrl=${URLEncoder.encode(url, "UTF-8")}"
  def getHelpAndContactUrl(key: String): String = helpAndContactHost + runModeConfiguration.getString(s"urls.help-and-contact.$key").getOrElse("")

  lazy val languageTranslationEnabled = runModeConfiguration.getBoolean("microservice.services.features.welsh-translation").getOrElse(true)
  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))
  def routeToSwitchLanguage = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val vatPaymentHistory = runModeConfiguration.getBoolean("microservice.services.features.get-payment-history").getOrElse(false)

  lazy val useEmacVatEnrolment = runModeConfiguration.getBoolean("microservice.services.features.emac-vat-enrolment").getOrElse(false)
  lazy val emacVatEnrolmentUrl = loadConfig("urls.external.emac.enrol")

  lazy val useEmacVatActivation = runModeConfiguration.getBoolean("microservice.services.features.emac-vat-activation").getOrElse(false)
  lazy val emacVatActivationUrl = loadConfig("urls.external.emac.activate")
  lazy val emacVatLostPinUrl = loadConfig("urls.external.emac.lostPin")
  lazy val googleTagManagerId = loadConfig(s"google-tag-manager.id")

  def sessionTimeoutInSeconds: Long = 900
  def sessionCountdownInSeconds: Int = 60
}
