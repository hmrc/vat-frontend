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

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.partials.vat.card.panel_info

import scala.collection.JavaConverters._

class PanelInfoViewSpec extends ViewBehaviours {

  def createView(optHasDirectDebit: Option[Boolean]): HtmlFormat.Appendable =
    panel_info(optHasDirectDebit)(messages)

  val expectedHeading = "Coronavirus (COVID-19) VAT deferral"

  val expectedBullets = List(
    "defer them without paying interest or penalties",
    "pay the VAT due as normal"
  )

  "PanelInfoView" when {
    "the user has direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(true)
      val doc: Document = asDocument(createView(optHasDirectDebit))

      "have the correct content" in {

        val expectedParagraphs: List[String] = List(
          "If you have VAT payments that are due between 20 March and 30 June 2020, you can choose to:",
          "You must continue to submit your returns as normal.",
          "If you choose to defer your VAT payments, you must pay the VAT due on or before 31 March 2021",
          "You do not need to tell HMRC that you are deferring your VAT payment, but you must contact your bank to cancel your Direct Debit as soon as possible."
        )

        val section: Element = doc.getElementById("vat-card-panel-info")

        val heading: Elements = section.select("h3")
        heading.text() mustBe expectedHeading

        val paragraphs: List[String] = section.select("p").asScala.map(_.text).toList

        (paragraphs zip expectedParagraphs).zipWithIndex foreach {
          case ((actual, expected), index) =>
            withClue(s"paragraph mismatch index $index") {
              actual mustBe expected
            }
        }

        val bullets: List[String] = section.select("li").asScala.map(_.text).toList
        (bullets zip expectedBullets).zipWithIndex foreach {
          case ((actual, expected), index) =>
            withClue(s"bullet points mismatch index $index") {
              actual mustBe expected
            }
        }

        val expectedTextOrder: List[String] =
          List(expectedHeading)
            .++(expectedParagraphs.take(1))
            .++(expectedBullets)
            .++(expectedParagraphs.drop(1))

        section.text mustBe expectedTextOrder.mkString(" ")
      }
    }

    "the user does not have direct debit" must {
      val optHasDirectDebit: Option[Boolean] = Some(false)
      val doc: Document = asDocument(createView(optHasDirectDebit))

      "have the correct content" in {

        val expectedParagraphs: List[String] = List(
          "If you have VAT payments that are due between 20 March and 30 June 2020, you can choose to:",
          "You must continue to submit your returns as normal.",
          "If you choose to defer your VAT payments, you must pay the VAT due on or before 31 March 2021",
          "You do not need to tell HMRC that you are deferring your VAT payment."
        )

        val section: Element = doc.getElementById("vat-card-panel-info")

        val heading: Elements = section.select("h3")
        heading.text() mustBe expectedHeading

        val paragraphs: List[String] = section.select("p").asScala.map(_.text).toList

        (paragraphs zip expectedParagraphs).zipWithIndex foreach {
          case ((actual, expected), index) =>
            withClue(s"paragraph mismatch index $index") {
              actual mustBe expected
            }
        }

        val bullets: List[String] = section.select("li").asScala.map(_.text).toList
        (bullets zip expectedBullets).zipWithIndex foreach {
          case ((actual, expected), index) =>
            withClue(s"bullet points mismatch index $index") {
              actual mustBe expected
            }
        }

        val expectedTextOrder: List[String] =
          List(expectedHeading)
            .++(expectedParagraphs.take(1))
            .++(expectedBullets)
            .++(expectedParagraphs.drop(1))

        section.text mustBe expectedTextOrder.mkString(" ")
      }
    }

    "the user's direct debit status is unknown" must {
      val optHasDirectDebit: Option[Boolean] = None
      val doc: Document = asDocument(createView(optHasDirectDebit))

      "have the correct content" in {

        val expectedParagraphs: List[String] = List(
          "If you have VAT payments that are due between 20 March and 30 June 2020, you can choose to:",
          "You must continue to submit your returns as normal.",
          "If you choose to defer your VAT payments, you must pay the VAT due on or before 31 March 2021",
          "You do not need to tell HMRC that you are deferring your VAT payment.",
          "If You normally pay by Direct Debit, you must contact your bank to cancel it as soon as possible."
        )

        val section: Element = doc.getElementById("vat-card-panel-info")

        val heading: Elements = section.select("h3")
        heading.text() mustBe expectedHeading

        val paragraphs: List[String] = section.select("p").asScala.map(_.text).toList

        (paragraphs zip expectedParagraphs).zipWithIndex foreach {
          case ((actual, expected), index) =>
            withClue(s"paragraph mismatch index $index") {
              actual mustBe expected
            }
        }

        val bullets: List[String] = section.select("li").asScala.map(_.text).toList
        (bullets zip expectedBullets).zipWithIndex foreach {
          case ((actual, expected), index) =>
            withClue(s"bullet points mismatch index $index") {
              actual mustBe expected
            }
        }

        val expectedTextOrder: List[String] =
          List(expectedHeading)
            .++(expectedParagraphs.take(1))
            .++(expectedBullets)
            .++(expectedParagraphs.drop(1))

        section.text mustBe expectedTextOrder.mkString(" ")
      }
    }
  }

}
