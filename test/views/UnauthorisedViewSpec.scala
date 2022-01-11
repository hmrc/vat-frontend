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

import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.unauthorised

class UnauthorisedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "unauthorised"

  def view(): Html = inject[unauthorised].apply(frontendAppConfig)(fakeRequest, messages)

  "Unauthorised view" must {

    behave like normalPage(view, messageKeyPrefix)

    "contain heading ID" in {
      val doc = asDocument(view())
      doc.getElementsByTag("h1").attr("id") mustBe "unauthorised"
    }

    "have the correct content" in {
      val doc = asDocument(view())
      doc.text() must include("Your VAT has not been added to this account")
      doc.text() must include("You may have used a different business tax account in the past to manage your taxes, duties or schemes online.")
      doc.text() must include("Continue")
    }

  }

}
