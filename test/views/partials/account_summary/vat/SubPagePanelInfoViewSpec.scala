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

package views.partials.vat.card

import config.FrontendAppConfig
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.Html
import views.ViewSpecBase
import views.behaviours.ViewBehaviours
import views.html.partials.account_summary.vat.panel_info

class SubPagePanelInfoViewSpec extends ViewBehaviours with ViewSpecBase with MockitoSugar {

  def view(eligibility: Option[String]): Html =
    inject[panel_info].apply(
      eligibility
    )(messages)

  "PanelInfoView" when {
    "the user has already signed up to the plan" must {

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(view(Some("Payment Exists")))
        val section: Element = doc.getElementById("vat-subpage-panel-info-payment-exists")
        val expectedParagraphs: List[String] = List(
          "Pay your deferred VAT: the new payment scheme",
          "You have joined the VAT deferral new payment scheme.",
          "You will not be able to monitor your deferred VAT Direct Debit payments through your business tax account. You must check these payments with your bank. If a Direct Debit payment is missed, HMRC will try to collect the payment again after 10 days.")
        section.text mustBe expectedParagraphs.mkString(" ")
      }
    }

    "the user is eligible to sign up to the plan" must {
      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(view(Some("Eligible")))
        val section: Element = doc.getElementById("vat-subpage-panel-info-eligible")
        val expectedParagraphs: List[String] = List(
          "Pay your deferred VAT: the new payment scheme",
          "You are eligible to join the",
          "VAT deferral new payment scheme (opens in new tab).",
          "You can join the scheme until 21 June 2021.")
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VATaccountSummary:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }
    }

    "the user's eligibility to sign up to the plan cannot be established" must {
      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(view(None))
        val section: Element = doc.getElementById("vat-subpage-panel-info")
        val expectedParagraphs: List[String] = List(
          "Pay your deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the",
          "VAT deferral new payment scheme (opens in new tab).")
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VATaccountSummary:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }
    }

    "the API call fails" must {
      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(view(Some("API Error")))
        val section: Element = doc.getElementById("vat-subpage-panel-info-api-error")
        val expectedParagraphs: List[String] = List(
          "Pay your deferred VAT: the new payment scheme",
          "Sorry, there is a problem with the service. Try again later.",
          "Find out more about the",
          "VAT deferral new payment scheme (opens in new tab).")
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VATaccountSummary:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }
    }
  }

}
