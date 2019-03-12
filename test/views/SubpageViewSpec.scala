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

package views

import models.VatDecEnrolment
import models.payment.{PaymentRecord, Successful}
import org.scalatest.mockito.MockitoSugar
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Vrn
import views.behaviours.ViewBehaviours
import views.html.subpage

class SubpageViewSpec extends ViewBehaviours with MockitoSugar {

  def messageKeyPrefix = "subpage"
  def vatVar = Html("<p>vatVar</p>")
  def doc = asDocument(createView())

  def serviceInfo = Html("<p id=\"partial-content\">This is service info</p>")
  def summary = Html("<p>This is an account summary.</p>")
  def sidebar = Html("<p>This is a sidebar.</p>")

  def vrn = "testVRN"
  def enrolment = VatDecEnrolment(Vrn(vrn), true)
  val history: List[PaymentRecord] = Nil

  def createView: () => HtmlFormat.Appendable = () => subpage(frontendAppConfig, summary, sidebar, enrolment, vatVar, history)(serviceInfo)(fakeRequest, messages)

  class Setup {
    val history: List[PaymentRecord] = Nil

    def createView = () => subpage(frontendAppConfig, summary, sidebar, enrolment, vatVar, history)(serviceInfo)(fakeRequest, messages)
    def doc = asDocument(createView())
  }

  "the main page" when {

    "loaded" should {


      "display the supplied account summary" in new Setup {
        doc.text() must include("This is an account summary.")
      }

      "display the supplied sidebar" in new Setup {
        doc.text() must include("This is a sidebar.")
      }

      "have the supplied service info" in new Setup {
        doc.getElementById("partial-content").text mustBe "This is service info"
      }

      "include the warning about the time to show paymnets" in new Setup {
        doc.text() must include("Payments will take 4 to 7 working days to show on this page.")
        doc.text() must include("Completed return amounts will take 1 to 2 days.")
      }

      "include the 'Submitted returns' heading" in new Setup {
        doc.text() must include("Submitted returns")
      }

      "include the 'view submitted returns' link" in new Setup {
        assertLinkById(doc, "view-submitted-returns", "View submitted returns (opens in a new window or tab)", s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng",
          "link - click:VATPreviouslySubmittedReturns:View submitted returns", expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "include the 'correct a mistake' link" in new Setup {
        assertLinkById(doc, "correct-a-mistake", "Correct a mistake (opens in a new window or tab)", "https://www.gov.uk/vat-corrections",
          "link - click:VATPreviouslySubmittedReturns:Correct a mistake", expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "include the 'Payments and repayments' heading" in new Setup {
        doc.text() must include("Payments and repayments")
      }

      "include the 'view payments and repayments' link" in new Setup {
        assertLinkById(doc, "view-payments-and-repayments", "View payments and repayments (opens in a new window or tab)",
          s"http://localhost:8080/portal/vat/trader/$vrn/account/overview?lang=eng",
          "link - click:VATPaymentsAndRepayments:View payments and repayments",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "include the 'vat certificate' link" in new Setup {
        doc.text() must include("Repayments are made to the account stated on your")
        assertLinkById(doc, "vat-certificate", "VAT certificate (opens in a new window or tab)", s"http://localhost:8080/portal/vat/trader/$vrn/certificate?lang=eng",
          "link - click:VATPaymentsAndRepayments:VAT certificate",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "include the 'change repayments account' link" in new Setup {
        assertLinkById(doc, "change-repayments-account", "Change your repayments account (opens in a new window or tab)",
          s"http://localhost:8080/portal/vat-variations/org/$vrn/introduction?lang=eng",
          "link - click:VATPaymentsAndRepayments:Change your repayments account",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "show the 'Get filing reminders' link" in new Setup {
        assertLinkById(doc, "get-filing-reminders", "Get filing reminders (opens in a new window or tab)",
          "https://foo.hmrc.gov.uk/eprompt/httpssl/changeVatEmailAddress.do", "link - click:VATMoreOptions:Get filing reminders",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "show the 'View VAT certificate' link" in new Setup {
        assertLinkById(doc, "view-vat-certificate", "View VAT certificate (opens in a new window or tab)",
          s"http://localhost:8080/portal/vat/trader/$vrn/certificate?lang=eng", "link - click:VATMoreOptions:View VAT certificate",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }

      "show the 'Paying by Direct Debit' link" in new Setup {
        assertLinkById(doc, "paying-by-direct-debit", "Paying by Direct Debit",
          "http://localhost:9733/business-account/help/vat/how-to-pay", "link - click:VATMoreOptions:Paying by Direct Debit")

      }

      "show the 'Add a VAT service' link" in new Setup {
        assertLinkById(doc, "add-vat-service", "Add a VAT service, e.g EC Sales List",
          "http://localhost:9730/business-account/add-tax/vat", "link - click:VATMoreOptions:Add a VAT service")
      }

      "display the vat var section" in {
        asDocument(createView()).text() must include("vatVar")
      }
    }

    "payment history is called" should {
      "display when a user has payment history" in new Setup {
        override val history = List(PaymentRecord(
          reference = "TEST56",
          amountInPence = 100,
          status = Successful,
          createdOn = "2018-10-21T08:00:00.000",
          taxType = "tax type"
        ))
        doc.html.contains("Your card payments in the last 7 days") mustBe true
        doc.html.contains("You paid £1 on 21 October 2018") mustBe true
        doc.html.contains("Your payment reference number is TEST56.") mustBe true
        doc.html.contains("It will take up to 7 days to update your balance after each payment.") mustBe true
      }

      "correctly format amount to £20.10" in new Setup {
        override val history = List(PaymentRecord(
          reference = "TEST58",
          amountInPence = 2010,
          status = Successful,
          createdOn = "2018-10-21T08:00:00.000",
          taxType = "tax type"
        ))
        doc.html.contains("Your card payments in the last 7 days") mustBe true
        doc.html.contains("You paid £20.10 on 21 October 2018") mustBe true
        doc.html.contains("Your payment reference number is TEST58.") mustBe true
      }

      "correctly format amount to £2,000.76" in new Setup {
        override val history = List(PaymentRecord(
          reference = "TEST58",
          amountInPence = 200076,
          status = Successful,
          createdOn = "2018-10-21T08:00:00.000",
          taxType = "tax type"
        ))
        doc.html.contains("Your card payments in the last 7 days") mustBe true
        doc.html.contains("You paid £2,000.76 on 21 October 2018") mustBe true
        doc.html.contains("Your payment reference number is TEST58.") mustBe true
      }

      "handle displaying 1000000000000" in new Setup {
        override val history = List(PaymentRecord(
          reference = "TEST58",
          amountInPence = 1000000000000L,
          status = Successful,
          createdOn = "2018-10-21T08:00:00.000",
          taxType = "tax type"
        ))
        doc.html.contains("Your card payments in the last 7 days") mustBe true
        doc.html.contains("You paid £10,000,000,000 on 21 October 2018") mustBe true
        doc.html.contains("Your payment reference number is TEST58.") mustBe true
      }

      "not display when a user has no payment history" in new Setup {
        doc.html.contains("Your card payments in the last 7 days") mustBe false
      }

    }

  }

}
