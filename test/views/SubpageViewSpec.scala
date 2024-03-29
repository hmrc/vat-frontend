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

package views

import models.payment.PaymentRecord
import models.{VatDecEnrolment, Vrn}
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.subpage

class SubpageViewSpec extends ViewBehaviours {

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
          "View submitted returns",
          s"http://localhost:8081/portal/vat-file/trader/$vrn/periods?lang=eng",
          expectedOpensInNewTab = false
        )
      }

      "include the 'Payments and repayments' heading" in {
        doc.text() must include("Payments and repayments")
      }

      "include the 'view payments and repayments' link" in {
        assertLinkById(
          doc,
          "view-payments-and-repayments",
          "View payments and repayments",
          s"http://localhost:8081/portal/vat/trader/$vrn/account/overview?lang=eng",
          expectedOpensInNewTab = false
        )
      }

      "include the 'change repayments account' link" in {
        assertLinkById(
          doc,
          "change-repayments-account",
          "Change your repayments account",
          s"http://localhost:8081/portal/vat-variations/org/$vrn/introduction?lang=eng",
          expectedOpensInNewTab = false
        )
      }

      "show the 'Get filing reminders' link" in {
        assertLinkById(
          doc,
          "get-filing-reminders",
          "Get filing reminders",
          "https://foo.hmrc.gov.uk/eprompt/httpssl/changeVatEmailAddress.do",
          expectedOpensInNewTab = false
        )
      }

      "show the 'View VAT certificate' link" in {
        assertLinkById(
          doc,
          "view-vat-certificate",
          "View VAT certificate",
          s"http://localhost:8081/portal/vat/trader/$vrn/certificate?lang=eng",
          expectedOpensInNewTab = false
        )
      }

      "show the 'Paying by Direct Debit' link" in {
        assertLinkById(
          doc,
          "paying-by-direct-debit",
          "Paying by Direct Debit",
          "http://localhost:9733/business-account/help/vat/how-to-pay"
        )

      }

      "show the 'how to pay vat' link" in {
        assertLinkById(
          doc,
          "how-to-pay-vat",
          "How to pay VAT",
          "https://www.gov.uk/pay-vat",
          expectedOpensInNewTab = false
        )

      }

      "show the 'get-refund' link" in {
        assertLinkById(
          doc,
          "get-refund",
          "Get a refund of VAT paid in another EU country",
          "https://www.gov.uk/guidance/vat-refunds-for-uk-businesses-buying-from-other-eu-countries",
          expectedOpensInNewTab = false
        )

      }

      "show the 'Add a VAT service' link" in {
        assertLinkById(
          doc,
          "add-vat-service",
          "Add a VAT service, e.g EC Sales List",
          "http://localhost:9730/business-account/add-tax/vat"
        )
      }

      "display the vat var section" in {
        asDocument(createView()).text() must include("vatVar")
      }
    }

  }

}
