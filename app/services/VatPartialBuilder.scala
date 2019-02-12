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
import connectors.models.AccountSummaryData
import javax.inject.{Inject, Singleton}
import play.api.i18n.Messages
import play.twirl.api.Html
import connectors.models._
import models.requests.AuthenticatedRequest
import views.html.partials.account_summary.vat.generic_error


@ImplementedBy(classOf[VatPartialBuilderImpl])
trait VatPartialBuilder {
  def buildReturnsPartial: Html
  def buildPaymentsPartial(vatAccountSummary: AccountSummaryData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
  def buildPaymentsPartialNew(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html
}

@Singleton
class VatPartialBuilderImpl @Inject() (appConfig: FrontendAppConfig) extends VatPartialBuilder {

  def buildReturnsPartial: Html = Html("")

  def buildPaymentsPartial(vatAccountSummary: AccountSummaryData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {
    if (vatAccountSummary.accountBalance.exists(accBal => accBal.amount.exists(amount => amount > 0))) {
      views.html.partials.vat.card.payments.payments_fragment_upcoming_bill(vatAccountSummary.accountBalance.get.amount.get.abs, appConfig, request.vatDecEnrolment)
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

    println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    println(accountData)
    println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

    accountData match {
      case VatData(accountSummaryData, calendar) => accountSummaryData match {
        case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) => {
          if (amount > 0) {
            views.html.partials.vat.card.payments.payments_fragment_upcoming_bill(amount.abs, appConfig, request.vatDecEnrolment)
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
      case VatNoData => Html("account.summary.no.balance.info.to.display")
      case _ => Html("generic error") // FIXME
    }

  }

}

/*
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