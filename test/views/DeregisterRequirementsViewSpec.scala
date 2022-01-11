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

package views

import play.twirl.api.{Html, HtmlFormat}
import views.behaviours.ViewBehaviours
import views.html.deregister_requirements

class DeregisterRequirementsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "deregister.requirements"
  val continueUrl = "hello/bye"

  def createView(): Html =
    inject[deregister_requirements].apply(frontendAppConfig, continueUrl)(HtmlFormat.empty)(
      fakeRequest,
      messages
    )

  "DeregisterRequirements view" should {
    behave like normalPage(createView, messageKeyPrefix)

    "contain heading ID" in {
      val doc = asDocument(createView())
      doc.getElementsByTag("h1").attr("id") mustBe "deregister-requirements"
    }

    "have the correct content" in {
      asDocument(createView()).text() must include(
        "After you deregister, you’ll need to:"
      )
      asDocument(createView()).text() must include("submit a final VAT return")
      asDocument(createView()).text() must include(
        "keep VAT records for 6 years"
      )
      assertLinkById(
        asDocument(createView()),
        "continue",
        "Continue",
        continueUrl,
        "link - click:VATderegisterRequirements:Continue"
      )
    }
  }
}
