/*
 * Copyright 2024 HM Revenue & Customs
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

package services.local

import config.FrontendAppConfig
import javax.inject.Inject
import models._
import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.twirl.api.{Html, HtmlFormat}
import views.html.partials.account_summary.vat._

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                     account_summary: account_summary,
                                     direct_debit_details: direct_debit_details,
                                     override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  case class MoneyPounds(value: BigDecimal, decimalPlaces: Int = 2, roundUp: Boolean = false) {
    def isNegative: Boolean = value < 0
    def quantity: String =
      s"%,.${decimalPlaces}f".format(
        value
          .setScale(decimalPlaces, if (roundUp) BigDecimal.RoundingMode.CEILING else BigDecimal.RoundingMode.FLOOR)
          .abs
      )
  }

  def renderableMoneyMessage(moneyPounds: MoneyPounds): Html = {
     val maybeMinus: String = if (moneyPounds.isNegative) "&minus;" else ""
    Html(s"$maybeMinus&pound;${moneyPounds.quantity}")
  }

  def getAccountSummaryView(maybeAccountData: Either[VatAccountFailure, Option[VatData]],
                            maybePayments: Either[PaymentRecordFailure.type, List[PaymentRecord]],
                            showCreditCardMessage: Boolean = true)
                           (implicit request: AuthenticatedRequest[_]): Html = {


    val breakdownLink = Some(appConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(request.vatDecEnrolment)))

    maybeAccountData match {
      case Right(Some(VatData(accountSummaryData, calendar, returnsToCompleteCount))) =>
        accountSummaryData match {
          case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) =>
            val isNotAnnual = calendar match {
              case Some(Calendar(Annually, _)) => false
              case _ => true
            }

            val directDebitContent = buildDirectDebitSection(calendar)

            if (amount < 0) {
              account_summary(
                Messages("account.in.credit", renderableMoneyMessage(MoneyPounds(amount.abs, 2))),
                accountSummaryData.openPeriods, appConfig, directDebitContent, breakdownLink, Messages("see.breakdown"),
                showRepaymentContent = isNotAnnual, shouldShowCreditCardMessage = showCreditCardMessage, maybePaymentHistory = maybePayments,
                noReturn = noReturnsBoolean(returnsToCompleteCount)
              )
            } else if (amount == 0) {
              account_summary(
                Messages("account.nothing.to.pay"),
                accountSummaryData.openPeriods, appConfig, directDebitContent, breakdownLink, Messages("view.statement"),
                shouldShowCreditCardMessage = showCreditCardMessage, maybePaymentHistory = maybePayments,
                noReturn = noReturnsBoolean(returnsToCompleteCount)
              )
            } else {
              account_summary(
                Messages("account.due", renderableMoneyMessage(MoneyPounds(amount.abs, 2))),
                accountSummaryData.openPeriods, appConfig, directDebitContent, breakdownLink, Messages("see.breakdown"),
                shouldShowCreditCardMessage = showCreditCardMessage, maybePaymentHistory = maybePayments,
                noReturn = noReturnsBoolean(returnsToCompleteCount)
              )
            }
          case _ => generic_error(appConfig.getPortalUrl("home")(Some(request.vatDecEnrolment)))
        }
      case Right(None) =>
        account_summary(Messages("account.summary.no.balance.info.to.display"), Seq.empty, appConfig, HtmlFormat.empty,
          shouldShowCreditCardMessage = showCreditCardMessage, maybePaymentHistory = maybePayments,
          noReturn = noReturnsBoolean(None))
      case _ => generic_error(appConfig.getPortalUrl("home")(Some(request.vatDecEnrolment)))
    }
  }

  private def buildDirectDebitSection(calendar: Option[Calendar])(implicit request: AuthenticatedRequest[_]): Html =
    calendar match {
      case Some(Calendar(filingFrequency, ActiveDirectDebit(details))) if filingFrequency != Annually =>
        direct_debit_details(details, appConfig)
      case Some(Calendar(filingFrequency, InactiveDirectDebit)) if filingFrequency != Annually =>
        prompt_to_activate_direct_debit(appConfig, request.vatDecEnrolment)
      case _ => HtmlFormat.empty
    }

  def noReturnsBoolean(returnsToCompleteCount: Option[Int]): Boolean = {
    returnsToCompleteCount match {
      case Some(value) => if(value == 0) true else false
      case _ => false
    }
  }
}
