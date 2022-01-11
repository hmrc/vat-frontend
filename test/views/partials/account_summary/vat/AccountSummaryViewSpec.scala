/*
 * Copyright 2022 HM Revenue & Customs
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

package views.partials.account_summary.vat

import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatEnrolment, VatVarEnrolment, Vrn}
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.{Html, HtmlFormat}
import views.ViewSpecBase
import views.html.partials.account_summary.vat.account_summary
import views.html.partials.payment_history
import java.time.LocalDateTime

class AccountSummaryViewSpec extends ViewSpecBase {

  def requestWithEnrolment(
    vatDecEnrolment: VatDecEnrolment,
    vatVarEnrolment: VatEnrolment
  ): AuthenticatedRequest[AnyContent] =
    AuthenticatedRequest[AnyContent](
      fakeRequest,
      "",
      vatDecEnrolment,
      vatVarEnrolment,
      "credId"
    )

  lazy val vatDecEnrolment: VatDecEnrolment = VatDecEnrolment(Vrn("vrn"), isActivated = true)
  lazy val vatVarEnrolment: VatVarEnrolment = VatVarEnrolment(Vrn("vrn"), isActivated = true)

  lazy val authenticatedRequest: AuthenticatedRequest[AnyContent] =
    requestWithEnrolment(vatDecEnrolment, vatVarEnrolment)

  lazy val testPaymentRecord: PaymentRecord = PaymentRecord(
    reference = "TEST1",
    amountInPence = 100,
    createdOn = LocalDateTime.parse("2018-10-21T08:00:00.000"),
    taxType = "tax type"
  )
  lazy val testPaymentHistory
    : Either[PaymentRecordFailure.type, List[PaymentRecord]] = Right(
    List(testPaymentRecord)
  )

  def returnView(): Html =
    inject[account_summary].apply(
      balanceInformation = "hello world",
      directDebitContent = Html(""),
      appConfig = frontendAppConfig,
      shouldShowCreditCardMessage = true,
      maybePaymentHistory = testPaymentHistory,
      noReturn = true,
      deferralContent = HtmlFormat.empty
    )(authenticatedRequest, messages)

  def view(): Html =
    inject[account_summary].apply(
      balanceInformation = "hello world",
      directDebitContent = Html(""),
      appConfig = frontendAppConfig,
      shouldShowCreditCardMessage = true,
      maybePaymentHistory = testPaymentHistory,
      noReturn = false,
      deferralContent = HtmlFormat.empty
    )(authenticatedRequest, messages)

  //todo more tests...
  "Account summary" when {
    "there is a user" should {
      "display the link to file a return" in {
        assertLinkById(
          asDocument(view()),
          "vat-file-return-link",
          "Complete VAT return",
          "http://localhost:8080/portal/vat-file/trader/vrn/return?lang=eng",
          "link - click:VATaccountSummary:Submit your VAT return"
        )
      }

      "display the heading and link to make a payment" in {
        assertLinkById(
          asDocument(view()),
          "vat-make-payment-link",
          "Make a VAT payment",
          "http://localhost:9732/business-account/vat/make-a-payment",
          "link - click:VATaccountSummary:Make a VAT payment"
        )
      }

      "render the provided balance information" in {
        asDocument(view()).getElementsByTag("p").text() must include(
          "hello world"
        )
      }
    }

    "display the heading and link to make a payment" in {
      assertLinkById(
        asDocument(returnView()),
        "vat-make-payment-link",
        "Make a VAT payment",
        "http://localhost:9732/business-account/vat/make-a-payment",
        "link - click:VATaccountSummary:Make a VAT payment"
      )
    }

    "Have conditional message for no returns" in {
      asDocument(returnView()).getElementsByTag("p").text() must include(
        "You have no returns to complete."
      )
    }

    "When user has returns do not display message" in {
      asDocument(view()).getElementsByTag("p").text() mustNot include(
        "You have no returns to complete."
      )
    }


    "must include the payment_history section" in {
      implicit val implicitMessages: Messages = messages
      view().toString() must include(
        payment_history(testPaymentHistory).toString
      )
    }
  }

}
