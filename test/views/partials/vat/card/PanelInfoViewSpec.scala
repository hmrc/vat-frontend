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

  def createView(optHasDirectDebit: Option[Boolean]): HtmlFormat.Appendable =
    panel_info(optHasDirectDebit, testAppConfig)(messages)

  val expectedHeading = "Delay VAT payments"

  "PanelInfoView" when {
    "the user has direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(true)
      val doc: Document = asDocument(createView(optHasDirectDebit))

      "have the correct content" in {

        val expectedParagraphs: List[String] = List(
          "You can delay (defer) VAT payments that are due between 20 March 2020 and 30 June 2020 if you cannot pay them because of coronavirus. You must pay them on or before 31 March 2021. Find out more about delaying VAT payments (opens in a new window or tab).",
          "If you normally pay by Direct Debit, you must contact your bank to cancel it as soon as possible.",
          "Important information",
          "You must continue to submit your VAT Returns as normal."
        )

        val section: Element = doc.getElementById("vat-card-panel-info")

        val heading: Elements = section.select("h3")
        heading.text() mustBe expectedHeading

        val expectedTextOrder: List[String] = List(expectedHeading) ++ expectedParagraphs

        assertLinkById(
          doc,
          "vat-deferal",
          "Find out more about delaying VAT payments (opens in a new window or tab).",
          "www.test.com",
          "link - click:VAT:VAT deferral",
          false,
          true
        )

        section.text mustBe expectedTextOrder.mkString(" ")
      }
    }

    "the user does not have direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(false)
      val doc: Document = asDocument(createView(optHasDirectDebit))

      "have the correct content" in {

        val expectedParagraphs: List[String] = List(
          "You can delay (defer) VAT payments that are due between 20 March 2020 and 30 June 2020 if you cannot pay them because of coronavirus. You must pay them on or before 31 March 2021. Find out more about delaying VAT payments (opens in a new window or tab).",
          "Important information",
          "You must continue to submit your VAT Returns as normal."
        )

        val section: Element = doc.getElementById("vat-card-panel-info")

        val heading: Elements = section.select("h3")
        heading.text() mustBe expectedHeading

        val expectedTextOrder: List[String] = List(expectedHeading) ++ expectedParagraphs

        assertLinkById(
          doc,
          "vat-deferal",
          "Find out more about delaying VAT payments (opens in a new window or tab).",
          "www.test.com",
          "link - click:VAT:VAT deferral",
          false,
          true
        )

        section.text mustBe expectedTextOrder.mkString(" ")
      }
    }

    "the user's direct debit status is unknown" must {
      val optHasDirectDebit: Option[Boolean] = None
      val doc: Document = asDocument(createView(optHasDirectDebit))

      "have the correct content" in {

        val expectedParagraphs: List[String] = List(
          "You can delay (defer) VAT payments that are due between 20 March 2020 and 30 June 2020 if you cannot pay them because of coronavirus. You must pay them on or before 31 March 2021. Find out more about delaying VAT payments (opens in a new window or tab).",
          "If you normally pay by Direct Debit, you must contact your bank to cancel it as soon as possible.",
          "Important information",
          "You must continue to submit your VAT Returns as normal."
        )

        val section: Element = doc.getElementById("vat-card-panel-info")

        val heading: Elements = section.select("h3")
        heading.text() mustBe expectedHeading

        val expectedTextOrder: List[String] = List(expectedHeading) ++ expectedParagraphs

        assertLinkById(
          doc,
          "vat-deferal",
          "Find out more about delaying VAT payments (opens in a new window or tab).",
          "www.test.com",
          "link - click:VAT:VAT deferral",
          false,
          true
        )

        section.text mustBe expectedTextOrder.mkString(" ")
      }
    }
  }

}
