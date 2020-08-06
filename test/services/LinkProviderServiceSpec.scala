/*
 * Copyright 2020 HM Revenue & Customs
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

import base.SpecBase
import models.VatData
import models.{Vrn, _}
import models.requests.AuthenticatedRequest
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest

class LinkProviderServiceSpec extends SpecBase {
  "The link provider service" when {

    val testLinkProvider: LinkProviderService = app.injector.instanceOf[LinkProviderService]

    lazy val vrn: Vrn = Vrn("1234567890")
    lazy val vatDecEnrolment: VatDecEnrolment = VatDecEnrolment(vrn, isActivated = true)

    def requestWithEnrolment(vatDecEnrolment: VatDecEnrolment): AuthenticatedRequest[AnyContent] = {
      AuthenticatedRequest[AnyContent](FakeRequest(), "", vatDecEnrolment, VatNoEnrolment(), "credId")
    }

    implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(FakeRequest())
    implicit val fakeRequestWithEnrolment: AuthenticatedRequest[AnyContent] = requestWithEnrolment(vatDecEnrolment)

    val emptyTestData = VatData(
      AccountSummaryData(
        Some(
          AccountBalance(Some(-1))
        ),
        None,
        Nil
      ),
      None,
      None
    )

    val makePaymentLink = Link(
      id = "vat-make-payment-link",
      title = "Make a VAT payment",
      href = "http://localhost:9732/business-account/vat/make-a-payment",
      ga = "link - click:VAT cards:Make a VAT payment"
    )

    val viewStatementLink = Link(
      id = "vat-view-statement-link",
      title = "View your VAT statement",
      href = "http://localhost:8080/portal/vat/trader/1234567890/account/overview?lang=eng",
      ga = "link - click:VAT cards:View your vat statement"
    )

    val setUpDirectDebitLink= Link(
      id = "vat-direct-debit-setup-link",
      title = "Set up a VAT Direct Debit",
      href = s"http://localhost:8080/portal/vat/trader/$vrn/directdebit?lang=eng",
      ga = "link - click:VAT cards:Set up a VAT Direct Debit"
    )

    def testDataForBalance(maybeBalance: Option[BigDecimal]): VatData =
      emptyTestData.copy(accountSummary = AccountSummaryData(accountBalance = Some(AccountBalance(maybeBalance)), None, Seq.empty))

    val testDataCanSetUpDirectDebit =  emptyTestData.copy(accountSummary = AccountSummaryData(accountBalance = Some(AccountBalance(Some(1))), None, Seq.empty),
      calendar = Some(Calendar(filingFrequency = Monthly, directDebit = InactiveDirectDebit)))

    "passed VAT data for an account in credit" should {
      "return None" in {
        testLinkProvider.determinePaymentAdditionalLinks(testDataForBalance(Some(-1))) mustBe None
      }
    }
    "passed VAT data for an account with a zero balance" should {
      "return None" in {
        testLinkProvider.determinePaymentAdditionalLinks(testDataForBalance(Some(0))) mustBe Some(
          List(
            viewStatementLink
          )
        )
      }
    }
    "passed VAT data for an account with no balance" should {
      "return None" in {
        testLinkProvider.determinePaymentAdditionalLinks(testDataForBalance(None)) mustBe None
      }
    }
    "passed VAT data for an account in debit that does not have the potential to set up a direct debit" should{
       "return a list containing the make payment link" in {
         testLinkProvider.determinePaymentAdditionalLinks(testDataForBalance(Some(1))) mustBe Some(
           List(
             makePaymentLink
           )
         )
       }
    }
    "passed VAT data for an account in debit that has the potential to set up a direct debit" should{
      "return a list containing the make payment link and the set up direct debit link" in {
        testLinkProvider.determinePaymentAdditionalLinks(testDataCanSetUpDirectDebit) mustBe Some(
          List(
            makePaymentLink,
            setUpDirectDebitLink
          )
        )
      }
    }
  }


}
