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

package views.partials.vat.card

import config.FrontendAppConfig
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.HtmlFormat
import views.ViewSpecBase
import views.behaviours.ViewBehaviours
import views.html.partials.vat.card.panel_info

import scala.collection.JavaConverters._

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
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section.text mustBe expectedParagraphs.mkString(" ")
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section.text mustBe expectedParagraphs.mkString(" ")
      }
    }

    "the user does not have direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(false)

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section.text mustBe expectedParagraphs.mkString(" ")
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section.text mustBe expectedParagraphs.mkString(" ")
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
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section1.text mustBe expectedParagraphs1.mkString(" ")
        section2.text mustBe expectedParagraphs2.mkString(" ")
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
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section.text mustBe expectedParagraphs.mkString(" ")
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information",
          "Paying deferred VAT",
          "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you must pay it in full by 31 March 2021.",
          "You can pay deferred VAT in part or in full any time up to 31 March 2021.",
          "If you cancelled your Direct Debit, set it up again so you do not miss a payment."
        )

        section.text mustBe expectedParagraphs.mkString(" ")
      }
    }
  }

}
