/*
 * Copyright 2019 HM Revenue & Customs
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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.deregister

class DeregisterViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "deregister"

  def createView = () => deregister(frontendAppConfig)(HtmlFormat.empty)(fakeRequest, messages)

  "Deregister view" should {
    behave like normalPage(createView, messageKeyPrefix)

    "have the correct content" in {
      asDocument(createView()).text() must include("Only deregister for VAT if you no longer need to submit VAT returns.")
      asDocument(createView()).text() must include("We’ll confirm your deregistration in around 3 weeks.")
      asDocument(createView()).getElementsByClass("panel").text() must include ("This will only cancel your VAT registration. You’ll need to stop other taxes and schemes separately.")
      assertLinkById(asDocument(createView()),"more","More about deregistering","https://www.gov.uk/vat-registration/cancel-registration","link - click:VATderegister:More")
      assertLinkById(asDocument(createView()), "continue", "Continue", "/business-account/vat/deregister/requirements", "link - click:VATderegister:Continue")
    }

  }



}
