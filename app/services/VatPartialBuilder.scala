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
import models._
import models.requests.AuthenticatedRequest
import play.api.i18n.{Lang, Messages}
import play.twirl.api.Html

@Singleton
class VatPartialBuilderImpl @Inject() (appConfig: FrontendAppConfig) extends VatPartialBuilder {

  override def buildReturnsPartial(vatAccountData: VatAccountData, vatEnrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {
    vatAccountData match {
      case VatData(account, _) if account.openPeriods.isEmpty => views.html.partials.vat.card.returns.no_returns(appConfig, Some(vatEnrolment))
      case VatData(account, _) if account.openPeriods.length == 1 => views.html.partials.vat.card.returns.one_return(appConfig, Some(vatEnrolment))
      case VatData(account, _) if account.openPeriods.length > 1 => views.html.partials.vat.card.returns.multiple_returns(appConfig, Some(vatEnrolment),
        account.openPeriods.length)
      case _ => Html("")
    }
  }

  override def buildPaymentsPartial(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {
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
        case _ => views.html.partials.vat.card.payments.payments_fragment_no_data()
      }
      case VatNoData => views.html.partials.vat.card.payments.payments_fragment_no_data()
      case VatGenericError => Html("")
      case VatEmpty => Html("")
      case VatUnactivated => Html("")
    }

  }

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
  def buildReturnsPartial(vatAccountData: VatAccountData, vatEnrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
  def buildPaymentsPartial(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
}
