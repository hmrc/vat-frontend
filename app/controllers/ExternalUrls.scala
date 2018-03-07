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

package controllers

import java.net.URLEncoder

//import model.HostContext
import play.api.Play
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.play.config.RunMode

object ExternalUrls extends RunMode{

  import play.api.Play.current
  val saPrefsHost = s"${Play.configuration.getString(s"govuk-tax.$env.sa-prefs.host").getOrElse("")}"
  val companyAuthHost = s"${Play.configuration.getString(s"govuk-tax.$env.company-auth.host").getOrElse("")}"
  val paymentsHost = Play.current.configuration.getString(s"govuk-tax.$env.payments-frontend.host").getOrElse("")
  val userDelegationHost = Play.current.configuration.getString(s"govuk-tax.$env.user-delegation-frontend.host").getOrElse("")
  val encrypter = ApplicationCrypto.QueryParameterCrypto
  val goPaperlessReturnLinkText = Messages("bt.message.return.paperless.link.text")

  // TODO: check if needed
  //val goPaperlessReturnUrl = loginCallback
  //val signIn = signInWithContinue(loginCallback)
  //val signInLocal = s"$companyAuthHost/gg/sign-in-local?continue=$loginCallback"
  // TODO: check if needed

  val manageUsers = s"$userDelegationHost/user-delegation/manage-users"
  val extraSecurityLink = s"$userDelegationHost/user-delegation/two-step-verification/register"
  val signInWithoutContinue = s"$companyAuthHost/gg/sign-in"

  val enrolmentManagementFrontendHMCETORequestNewActivationCodeUrl = s"${Play.configuration.getString(s"govuk-tax.$env.enrolment-management-frontend.hmce-to.request-new-activation-code.url").getOrElse("")}"
  val enrolmentManagementFrontendHMCETOGetAccessTaxSchemeUrl = s"${Play.configuration.getString(s"govuk-tax.$env.enrolment-management-frontend.hmce-to.get-access-tax-scheme.url").getOrElse("")}"

  val enrolmentManagementFrontendHMCERORequestNewActivationCodeUrl = s"${Play.configuration.getString(s"govuk-tax.$env.enrolment-management-frontend.hmce-ro.request-new-activation-code.url").getOrElse("")}"
  val enrolmentManagementFrontendHMCEROGetAccessTaxSchemeUrl = s"${Play.configuration.getString(s"govuk-tax.$env.enrolment-management-frontend.hmce-ro.get-access-tax-scheme.url").getOrElse("")}"

  def signInWithContinue(url: String) = s"$signInWithoutContinue?continue=$url"
  def signOutWithContinue(url: String) = s"$companyAuthHost/gg/sign-out?continue=$url"

  private def buildPaperlessLink(returnLinkText: String, returnUrl:String) = {
    def encryptAndEncode(s: String) = URLEncoder.encode(encrypter.encrypt(PlainText(s)).value, "UTF-8")
    val encryptedReturnLinkText = encryptAndEncode(returnLinkText)
    val encryptedReturnUrl = encryptAndEncode(returnUrl)
    s"$saPrefsHost/paperless/choose?returnUrl=$encryptedReturnUrl&returnLinkText=$encryptedReturnLinkText"
  }

  // TODO: remove(???)
  //def buildGoPaperlessLink(hostContextOption: Option[HostContext]) = hostContextOption match {
  //  case Some(hostContext) => buildPaperlessLink(hostContext.returnLinkText, hostContext.returnUrl)
  //  case _ => buildPaperlessLink(goPaperlessReturnLinkText, goPaperlessReturnUrl)
  //}
}
