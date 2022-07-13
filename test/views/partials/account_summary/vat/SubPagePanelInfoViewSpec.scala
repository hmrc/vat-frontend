/*
 * Copyright 2022 HM Revenue & Customs
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
import play.twirl.api.Html
import views.ViewSpecBase
import views.behaviours.ViewBehaviours
import views.html.partials.account_summary.vat.panel_info

class SubPagePanelInfoViewSpec extends ViewBehaviours with ViewSpecBase {

  def view(): Html =
    inject[panel_info].apply(
    )(messages)

  "PanelInfoView" when {
    "the user has already signed up to the plan" must {

      "the user's eligibility to sign up to the plan cannot be established" must {
        "have the correct content for COVID-19 post deferral period" in {
          val doc: Document = asDocument(view())
          val section: Element = doc.getElementById("vat-subpage-panel-info")
          val expectedParagraphs: List[String] = List(
            "Pay your deferred VAT: the new payment scheme",
            "If you deferred paying VAT that was due between 20 March 2020 and 30 June 2020, you may be able to join the",
            "VAT deferral new payment scheme.")
          section.text mustBe expectedParagraphs.mkString(" ")

          assertLinkById(
            doc,
            linkId = "vat-delayed-link",
            expectedText = "VAT deferral new payment scheme",
            expectedUrl = "https://www.gov.uk/guidance/deferral-of-vat-payments-due-to-coronavirus-covid-19",
            expectedOpensInNewTab = false
          )
        }
      }
    }
  }
}
