/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{VatDecEnrolment, Vrn}
import models.payment.PaymentRecord
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.{Html, HtmlFormat}
import views.behaviours.ViewBehaviours
import views.html.subpage

class SubpageViewSpec extends ViewBehaviours with MockitoSugar {

  def messageKeyPrefix = "subpage"

  def vatVar: Html = Html("<p>vatVar</p>")

  def doc: Document = asDocument(createView())

  def serviceInfo: Html = Html("<p id=\"partial-content\">This is service info</p>")

  def summary: Html = Html("<p>This is an account summary.</p>")

  def sidebar: Html = Html("<p>This is a sidebar.</p>")

  def vrn = "testVRN"

  def enrolment: VatDecEnrolment = VatDecEnrolment(Vrn(vrn), true)

  def createView(): Html =
    inject[subpage].apply(frontendAppConfig, summary, sidebar, enrolment, vatVar)(
      serviceInfo
    )(fakeRequest, messages)

  class Setup {
    val history: List[PaymentRecord] = Nil

    def createView(): Html =
      inject[subpage].apply(frontendAppConfig, summary, sidebar, enrolment, vatVar)(
        serviceInfo
      )(fakeRequest, messages)

    def doc: Document = asDocument(createView())
  }

  "the aggregated subpage " should {

    behave like normalPage(createView, messageKeyPrefix)

    "contain heading ID" in {
      val doc = asDocument(createView())
      doc.getElementsByTag("h1").attr("id") mustBe "your-vat"
    }
  }

  "the main page" when {

    "loaded" should {

      "display the supplied account summary" in {
        doc.text() must include("This is an account summary.")
      }

      "display the supplied sidebar" in {
        doc.text() must include("This is a sidebar.")
      }

      "have the supplied service info" in {
        doc.getElementById("partial-content").text mustBe "This is service info"
      }

      "include the warning about the time to show payments" in {
        val paragraph = doc.getElementById("payments-notice").text

        paragraph mustBe Seq(
          "Payments will take up to 7 working days to show, depending on how you pay.",
          "After you complete your return your tax calculation will take up to 2 days."
        ).mkString(" ")
      }

      "include the 'Submitted returns' heading" in {
        doc.text() must include("Submitted returns")
      }
      
      "include the 'view submitted returns' link" in {
        assertLinkById(
          doc,
          "view-submitted-returns",
          "View submitted returns (opens in new tab)",
          s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng",
          "link - click:VATPreviouslySubmittedReturns:View submitted returns",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "include the 'Payments and repayments' heading" in {
        doc.text() must include("Payments and repayments")
      }

      "include the 'view payments and repayments' link" in {
        assertLinkById(
          doc,
          "view-payments-and-repayments",
          "View payments and repayments (opens in new tab)",
          s"http://localhost:8080/portal/vat/trader/$vrn/account/overview?lang=eng",
          "link - click:VATPaymentsAndRepayments:View payments and repayments",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "include the 'change repayments account' link" in {
        assertLinkById(
          doc,
          "change-repayments-account",
          "Change your repayments account (opens in new tab)",
          s"http://localhost:8080/portal/vat-variations/org/$vrn/introduction?lang=eng",
          "link - click:VATPaymentsAndRepayments:Change your repayments account",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "show the 'Get filing reminders' link" in {
        assertLinkById(
          doc,
          "get-filing-reminders",
          "Get filing reminders (opens in new tab)",
          "https://foo.hmrc.gov.uk/eprompt/httpssl/changeVatEmailAddress.do",
          "link - click:VATMoreOptions:Get filing reminders",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "show the 'View VAT certificate' link" in {
        assertLinkById(
          doc,
          "view-vat-certificate",
          "View VAT certificate (opens in new tab)",
          s"http://localhost:8080/portal/vat/trader/$vrn/certificate?lang=eng",
          "link - click:VATMoreOptions:View VAT certificate",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "show the 'Paying by Direct Debit' link" in {
        assertLinkById(
          doc,
          "paying-by-direct-debit",
          "Paying by Direct Debit",
          "http://localhost:9733/business-account/help/vat/how-to-pay",
          "link - click:VATMoreOptions:Paying by Direct Debit"
        )

      }

      "show the 'how to pay vat' link" in {
        assertLinkById(
          doc,
          "how-to-pay-vat",
          "How to pay VAT (opens in new tab)",
          "https://www.gov.uk/pay-vat",
          "link - click:VATMoreOptions:How to pay VAT",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )

      }

      "show the 'get-refund' link" in {
        assertLinkById(
          doc,
          "get-refund",
          "Get a refund of VAT paid in another EU country (opens in new tab)",
          "https://www.gov.uk/guidance/vat-refunds-for-uk-businesses-buying-from-other-eu-countries",
          "link - click:VATMoreOptions:Get a refund of VAT paid in another EU country",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )

      }

      "show the 'Add a VAT service' link" in {
        assertLinkById(
          doc,
          "add-vat-service",
          "Add a VAT service, e.g EC Sales List",
          "http://localhost:9730/business-account/add-tax/vat",
          "link - click:VATMoreOptions:Add a VAT service"
        )
      }

      "display the vat var section" in {
        asDocument(createView()).text() must include("vatVar")
      }
    }

  }

}
