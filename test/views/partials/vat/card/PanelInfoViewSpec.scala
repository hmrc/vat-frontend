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
import org.scalatest.mockito.MockitoSugar
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
          "Important information ",
          "The VAT deferral period has ended. ",
          "VAT bills with a payment due date on or after 1 July 2020 must be paid on time and in full. If you cancelled your Direct Debit, set it up again so you do not miss a payment. You still need to submit VAT Returns, even if your business has temporarily closed."
        )

        section.text mustBe expectedParagraphs.mkString("")
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information ",
          "The VAT deferral period is ending. ",
          "VAT bills with a payment due date on or after 1 July 2020 must be paid on time and in full. If you cancelled your Direct Debit, set it up again so you do not miss a payment. You still need to submit VAT Returns, even if your business has temporarily closed."
        )

        section.text mustBe expectedParagraphs.mkString("")
      }
    }

    "the user does not have direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(false)

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information ",
          "The VAT deferral period has ended. ",
          "VAT bills with a payment due date on or after 1 July 2020 must be paid on time and in full. If you cancelled your Direct Debit, set it up again so you do not miss a payment. You still need to submit VAT Returns, even if your business has temporarily closed."
        )

        section.text mustBe expectedParagraphs.mkString("")
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information ",
          "The VAT deferral period is ending. ",
          "VAT bills with a payment due date on or after 1 July 2020 must be paid on time and in full. If you cancelled your Direct Debit, set it up again so you do not miss a payment. You still need to submit VAT Returns, even if your business has temporarily closed."
        )

        section.text mustBe expectedParagraphs.mkString("")
      }
    }

    "the user's direct debit status is unknown" must {
      val optHasDirectDebit: Option[Boolean] = None

      "have the correct content for COVID-19 post deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, true))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information ",
          "The VAT deferral period has ended. ",
          "VAT bills with a payment due date on or after 1 July 2020 must be paid on time and in full. If you cancelled your Direct Debit, set it up again so you do not miss a payment. You still need to submit VAT Returns, even if your business has temporarily closed."
        )

        section.text mustBe expectedParagraphs.mkString("")
      }

      "have the correct content for COVID-19 pre deferral period" in {
        val doc: Document = asDocument(createView(optHasDirectDebit, false))
        val section: Element = doc.getElementById("vat-card-panel-info")
        val expectedParagraphs: List[String] = List(
          "Important information ",
          "The VAT deferral period is ending. ",
          "VAT bills with a payment due date on or after 1 July 2020 must be paid on time and in full. If you cancelled your Direct Debit, set it up again so you do not miss a payment. You still need to submit VAT Returns, even if your business has temporarily closed."
        )

        section.text mustBe expectedParagraphs.mkString("")
      }
    }
  }

}
