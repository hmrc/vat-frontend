/*
 * Copyright 2023 HM Revenue & Customs
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

import play.twirl.api.{Html, HtmlFormat}
import views.behaviours.ViewBehaviours
import views.html.deregister

class DeregisterViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "deregister"

  def createView(): Html = inject[deregister].apply(frontendAppConfig)(HtmlFormat.empty)(fakeRequest, messages)

  "Deregister view" should {
    behave like normalPage(createView, messageKeyPrefix)

    "contain heading ID" in {
      val doc = asDocument(createView())
      doc.getElementsByTag("h1").attr("id") mustBe "deregister"
    }

    "have the correct content" in {
      asDocument(createView()).text() must include(
        "Only deregister for VAT if you have (any of the following):"
      )
      asDocument(createView()).text() must include(
        "stopped making or trading VAT taxable supplies"
      )
      asDocument(createView()).text() must include(
        "joined a VAT group"
      )
      asDocument(createView()).text() must include(
        "VAT taxable turnover below the deregistration threshold of £83,000"
      )
      asDocument(createView()).text() must include(
        "We will confirm your deregistration in around 3 weeks."
      )
      asDocument(createView()).text() must include(
        "This will only cancel your VAT registration. You must deregister for other taxes, duties or schemes separately"
      )
      assertLinkById(
        asDocument(createView()),
        "continue",
        "Continue",
        "/business-account/vat/deregister/requirements"
      )
    }

  }

}
