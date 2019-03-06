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

package services

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import connectors.models.{AccountSummaryData, _}
import javax.inject.{Inject, Singleton}
import models.{ActiveDirectDebit, Annually, Calendar, InactiveDirectDebit}
import models.requests.AuthenticatedRequest
import play.api.i18n.Messages
import play.twirl.api.Html

@Singleton
class VatPartialBuilderImpl @Inject() (appConfig: FrontendAppConfig) extends VatPartialBuilder {

  def buildReturnsPartial: Html = Html("Returns - WORK IN PROGRESS")

  def buildPaymentsPartial(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {

    accountData match {
      case VatData(accountSummaryData, calendar) => accountSummaryData match {

        case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) => {
          if (amount > 0) {
            val hasDD: Boolean = hasDirectDebit(calendar)
            views.html.partials.vat.card.payments.payments_fragment_upcoming_bill(amount.abs, hasDD, appConfig, request.vatDecEnrolment)
          }
          else if (amount == 0) {
            views.html.partials.vat.card.payments.payments_fragment_no_tax(appConfig)
          }
          else if (amount < 0) {
            views.html.partials.vat.card.payments.payments_fragment_just_credit(amount.abs, appConfig)
          }
          else {
            Html("")
          }
        }
        case _ => Html("generic error") // FIXME
      }
      case VatNoData => views.html.partials.vat.card.payments.payments_fragment_no_data()
      case _ => Html("generic error") // FIXME (VatEmpty)
    }

  }

  // TODO: refactor if needed based on the private method buildDirectDebitSection
  private def hasDirectDebit(calendar: Option[Calendar])(implicit request: AuthenticatedRequest[_]): Boolean = {
    calendar match {
      case Some(Calendar(filingFrequency, ActiveDirectDebit(details))) if filingFrequency != Annually => true
      case Some(Calendar(filingFrequency,InactiveDirectDebit)) if filingFrequency != Annually         => false
      case _ => false
    }
  }

}

@ImplementedBy(classOf[VatPartialBuilderImpl])
trait VatPartialBuilder {
  def buildReturnsPartial: Html
  def buildPaymentsPartial(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
}
