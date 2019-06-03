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

package controllers.helpers

import javax.inject.Inject
import config.FrontendAppConfig
import connectors.models._
import models._
import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.{Html, HtmlFormat}
import services.VatService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.views.formatting.Money.pounds
import utils.EmacUrlBuilder
import views.html.partials.account_summary.vat._

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                     vatService: VatService,
                                     emacUrlBuilder: EmacUrlBuilder,
                                     override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getAccountSummaryView(maybeAccountData: Either[VatAccountFailure, Option[VatData]],
                                                 maybePayments: Either[PaymentRecordFailure.type, List[PaymentRecord]],
                                                 showCreditCardMessage: Boolean = true)
                                                (implicit request: AuthenticatedRequest[_]): Html = {

    implicit def hc(implicit rh: RequestHeader): HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    val breakdownLink = Some(appConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(request.vatDecEnrolment)))

    //todo refactor
    (maybeAccountData, maybePayments) match {
      case (Right(Some(VatData(accountSummaryData, calendar))), Right(payments)) =>
        accountSummaryData match {
          case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) =>
            val isNotAnnual = calendar match {
              case Some(Calendar(Annually, _)) => false
              case _ => true
            }

            val directDebitContent = buildDirectDebitSection(calendar)

            if (amount < 0) {
              account_summary(
                Messages("account.in.credit", pounds(amount.abs, 2)),
                accountSummaryData.openPeriods, appConfig, directDebitContent, breakdownLink, Messages("see.breakdown"),
                showRepaymentContent = isNotAnnual, shouldShowCreditCardMessage = showCreditCardMessage, paymentHistory = payments
              )
            } else if (amount == 0) {
              account_summary(
                Messages("account.nothing.to.pay"),
                accountSummaryData.openPeriods, appConfig, directDebitContent, breakdownLink, Messages("view.statement"),
                shouldShowCreditCardMessage = showCreditCardMessage, paymentHistory = payments
              )
            } else {
              account_summary(
                Messages("account.due", pounds(amount.abs, 2)),
                accountSummaryData.openPeriods, appConfig, directDebitContent, breakdownLink, Messages("see.breakdown"),
                shouldShowCreditCardMessage = showCreditCardMessage, paymentHistory = payments
              )
            }
          case _ => generic_error(appConfig.getPortalUrl("home")(Some(request.vatDecEnrolment)))
        }
      case (Right(None), Right(payments)) =>
        account_summary(Messages("account.summary.no.balance.info.to.display"), Seq.empty, appConfig, HtmlFormat.empty,
          shouldShowCreditCardMessage = showCreditCardMessage, paymentHistory = payments)
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

}
