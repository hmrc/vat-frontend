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
import views.html.partials.account_summary.vat.{direct_debit_details, prompt_to_activate_direct_debit}


@ImplementedBy(classOf[VatPartialBuilderImpl])
trait VatPartialBuilder {
  def buildReturnsPartial: Html
  def buildPaymentsPartial(vatAccountSummary: AccountSummaryData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
  def buildPaymentsPartialNew(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
}

@Singleton
class VatPartialBuilderImpl @Inject() (appConfig: FrontendAppConfig) extends VatPartialBuilder {

  def buildReturnsPartial: Html = Html("Returns - WORK IN PROGRESS")

  def buildPaymentsPartial(vatAccountSummary: AccountSummaryData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {
    if (vatAccountSummary.accountBalance.exists(accBal => accBal.amount.exists(amount => amount > 0))) {
      val hasDD: Boolean = hasDirectDebit(calendar)
      views.html.partials.vat.card.payments.payments_fragment_upcoming_bill(vatAccountSummary.accountBalance.get.amount.get.abs, hasDD, appConfig, request.vatDecEnrolment)
    }
    else if (vatAccountSummary.accountBalance.exists(accBal => accBal.amount.exists(amount => amount == 0))) {
      views.html.partials.vat.card.payments.payments_fragment_no_tax(appConfig)
    }
    else if (vatAccountSummary.accountBalance.exists(accBal => accBal.amount.exists(amount => amount < 0))) {
      views.html.partials.vat.card.payments.payments_fragment_just_credit(vatAccountSummary.accountBalance.get.amount.get.abs, appConfig)
    }
    else {
      Html("generic error") // FIXME
    }
  }

  def buildPaymentsPartialNew(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {

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
            Html("aa")
          }
        }
        case _ => Html("generic error") // FIXME
      }
      case VatNoData => views.html.partials.vat.card.payments.payments_fragment_no_data()
      case _ => Html("generic error") // FIXME
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




/*
TODO: remove
  def buildPaymentsPartial(accountSummary:SaAccountSummary, futureLiabilities: Seq[FutureLiability])(implicit messages: Messages, userProfile: UserProfile): Html = {
    if (accountSummary.totalAmountDueToHmrc.exists(amountDue => amountDue.amount > 0)){
      payments_fragment_overdue_bill(accountSummary.totalAmountDueToHmrc.get.amount)
    } else if (accountSummary.amountHmrcOwe.exists(_ > 0)){
      getCreditFragment(futureLiabilities, accountSummary.amountHmrcOwe.get)
    } else {
      val nextBill = getNextBill(futureLiabilities)
      if (nextBill.exists(bill => bill.amount > 0)) {
        val billDate = nextBill.get.date
        payments_fragment_upcoming_bill(nextBill.get.amount, billDate, getDaysLeftFragment(billDate))
      } else {
        payments_fragment_no_tax()
      }
    }
  }
 */