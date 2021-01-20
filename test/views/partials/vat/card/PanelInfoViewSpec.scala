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

  def createView(optHasDirectDebit: Option[Boolean], pastDeferralPeriod: Boolean): HtmlFormat.Appendable =
    panel_info(optHasDirectDebit, testAppConfig, pastDeferralPeriod)(messages)

  val expectedHeading = "Delay VAT payments"

  "PanelInfoView" when {
    "the user has direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(true)

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
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
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
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
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
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
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
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
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
        )

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
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
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
          "Paying deferred VAT: a new scheme",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you can opt into the VAT deferral new payment scheme (opens in new tab).",
          "Instead of paying the full amount by 31 March 2021, you can make smaller, interest free payments until 31 March 2022.",
          "You can opt into the new scheme from early 2021."
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
