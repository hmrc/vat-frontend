/*
 * Copyright 2018 HM Revenue & Customs
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

  private val customsRootUrl = loadConfig(s"govuk-tax.$env.externalLinks.customsUrl")
  def customsUrl(path: String) = s"$customsRootUrl/$path"

  private lazy val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "vatfrontend"

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  lazy val authUrl = baseUrl("auth")
  lazy val btaUrl = baseUrl("business-tax-account")
  lazy val ctUrl = baseUrl("ct") // TODO: remove this(?)
  lazy val vatUrl = baseUrl("vat")

  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")
  //val loginCallback = Play.configuration.getString(s"govuk-tax.$env.login-callback.url").getOrElse(routes.BusinessTaxHomeController.home().url)
  val loginCallback = runModeConfiguration.getString(s"govuk-tax.$env.login-callback.url").getOrElse(businessAccountHomeUrl)

  private lazy val businessAccountHost = runModeConfiguration.getString("urls.business-account.host").getOrElse("")
  lazy val businessAccountHomeUrl = businessAccountHost + "/business-account"
  lazy val manageAccountUrl = businessAccountHost + "/business-account/manage-account"

  val paymentsHost = runModeConfiguration.getString("urls.payment-frontend.host").getOrElse("")
    //Play.current.configuration.getString(s"govuk-tax.$env.payments-frontend.host").getOrElse("")
  private lazy val portalHost = loadConfig(s"urls.external.portal.host")

  def getUrl(key: String): String = loadConfig(s"urls.$key")
  def getGovUrl(key: String): String = loadConfig(s"urls.external.govuk.$key")
  def getFormsUrl(key: String): String = loadConfig(s"urls.forms.$key")
  def getBusinessAccountUrl(key: String): String = businessAccountHost + loadConfig(s"urls.business-account.$key")
  def getPortalUrl(key: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): String =
    buildPortalUrl(portalHost + loadConfig(s"urls.external.portal.$key"))(vatEnrolment)
  def getHelpAndContactUrl(subpage: String): String = s"$businessAccountHost/help/$subpage"
  def getReturnUrl(url:String) = s"returnUrl=${URLEncoder.encode(url, "UTF-8")}"

  lazy val languageTranslationEnabled = runModeConfiguration.getBoolean("microservice.services.features.welsh-translation").getOrElse(true)
  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))
  def routeToSwitchLanguage = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)
}
