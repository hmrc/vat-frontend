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

package views

import views.behaviours.ViewBehaviours
import views.html.unauthorised

class UnauthorisedViewSpec extends ViewBehaviours {

  def view = () => unauthorised(frontendAppConfig)(fakeRequest, messages)

  "Unauthorised view" must {

    behave like normalPage(view, "unauthorised")
    "have the correct content" in {
      val doc = asDocument(view())
      doc.text() must include ("You can’t see this page")
      doc.text() must include ("You haven’t added Corporation Tax to this account.")
      doc.text() must include ("Make sure you’re signed in with the correct user ID.")
    }

  }
}
