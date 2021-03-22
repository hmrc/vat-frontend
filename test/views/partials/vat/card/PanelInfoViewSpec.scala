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
import play.twirl.api.HtmlFormat
import views.ViewSpecBase
import views.behaviours.ViewBehaviours
import views.html.partials.vat.card.panel_info

class PanelInfoViewSpec extends ViewBehaviours with ViewSpecBase with MockitoSugar {
  lazy val testAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  when(testAppConfig.getGovUrl("deferal")).thenReturn("www.test.com")

  def createView(optHasDirectDebit: Option[Boolean], pastDeferralPeriod: Boolean, eligibility: Option[String] = None): HtmlFormat.Appendable =
    panel_info(optHasDirectDebit, testAppConfig, pastDeferralPeriod, eligibility)(messages)

  val expectedHeading = "Delay VAT payments"

  "PanelInfoView" when {
    "the user has direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(true)

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme.")
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme."
        )
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }
    }

    "the user does not have direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(false)

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme."
        )
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme."
        )
        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }

      "have the correct content for COVID-19 post deferral period and vat outage content when feature flag is set to true" in {

        when(testAppConfig.vatContentOutage).thenReturn(true)
        val doc: Document = asDocument(createView(optHasDirectDebit, pastDeferralPeriod = true))
        val section1: Element = doc.getElementById("vat-card-panel-info1")
        val section2: Element = doc.getElementById("vat-card-panel-info2")

        val expectedParagraphs1: List[String] = List(
          "Important information",
          "Sorry, there is a problem with this service",
          "Your payments and liabilities have not been updated since 3 November.",
          "This is due to system maintenance.",
          "We’ll resume normal service as soon as possible.")

        val expectedParagraphs2: List[String] = List(
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme."        )

        section1.text mustBe expectedParagraphs1.mkString(" ")
        section2.text mustBe expectedParagraphs2.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )

      }
    }

    "the user's direct debit status is unknown" must {
      val optHasDirectDebit: Option[Boolean] = None

      "have the correct content for COVID-19 post deferral period" in {
        when(testAppConfig.vatContentOutage).thenReturn(false)
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme."        )

        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )

      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: the new payment scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments. The number of instalments depends on when you join the scheme."
        )

        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )

      }

      "have the correct content for COVID-19 post deferral period eligibility API returns Payment Exists" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false, Some("Payment Exists")))
        val section: Element = doc.getElementById("vat-card-panel-info-payment-exists")
        val expectedParagraphs: List[String] = List(
          "Pay your deferred VAT: the new payment scheme",
          "You have joined the VAT deferral new payment scheme.",
          "You will not be able to monitor your deferred VAT Direct Debit payments through your business tax account. You must check " +
            "these payments with your bank. If a Direct Debit payment is missed, HMRC will try to collect the payment again after 10 days.")
        section.text mustBe expectedParagraphs.mkString(" ")
      }

      "have the correct content for COVID-19 post deferral period eligibility API returns Eligible" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false, Some("Eligible")))
        val section: Element = doc.getElementById("vat-card-panel-info-eligible")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Pay your deferred VAT: the new payment scheme",
          "You are eligible to join the VAT deferral new payment scheme (opens in new tab).",
          "You can join the scheme until 21 June 2021.",
          "Instead of paying the full amount of your deferred VAT immediately, you can pay in smaller, interest free instalments." +
            " The number of instalments depends on when you join the scheme.",
          "You may have to pay interest or a penalty if you do not join the scheme or pay in full."
        )

        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }

      "have the correct content for COVID-19 post deferral period eligibility API returns API Error" in {
          val doc: Document = asDocument(createView(optHasDirectDebit, false, Some("API Error")))
        val section: Element = doc.getElementById("vat-card-panel-info-api-error")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Pay your deferred VAT: the new payment scheme",
          "Sorry, there is a problem with the service. Try again later.",
          "Find out more about the VAT deferral new payment scheme (opens in new tab)."
        )

        section.text mustBe expectedParagraphs.mkString(" ")

        assertLinkById(
          doc,
          linkId = "vat-delayed-link",
          expectedText = "VAT deferral new payment scheme (opens in new tab)",
          expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
          expectedGAEvent = "link - click:VAT cards:VAT deferral new payment scheme",
          expectedOpensInNewTab = true
        )
      }
    }
  }

}
