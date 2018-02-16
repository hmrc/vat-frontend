/*
 * Copyright 2018 HM Revenue & Customs
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

package views.partials

import views.ViewSpecBase
import views.html.partials.not_activated

class NotActivatedViewSpec extends ViewSpecBase {

  val activateUrl = "http://activate.url"
  val resetCodeUrl = "http://reset-code.url"

  def view = () => not_activated(activateUrl, resetCodeUrl)(fakeRequest, messages)

  "NotActivated" should {
    "have a link to activate using the provided url" in {
      val doc = asDocument(view())
      assertLinkById(
        doc,
        "ir-ct",
        "Activate",
        activateUrl,
        "corporation-tax:Click:Activate your CT account"
      )
      doc.text() must include("(opens in HMRC online)")
    }

    "have the need activation code content" in {
      asDocument(view()).text() must include("You’ll need the activation code we sent you in the post.")
    }

    "have a link to reset the activation code using the provided url" in {
      assertLinkById(
        asDocument(view()),
        "ir-ct-reset",
        "I’ve lost my activation code",
        resetCodeUrl,
        "business-tax-home:Click:Lost activation code for CT"
      )
    }

    "have the activation period message" in {
      asDocument(view()).text() must include("Activation takes up to 72 hours.")
    }
  }

}
