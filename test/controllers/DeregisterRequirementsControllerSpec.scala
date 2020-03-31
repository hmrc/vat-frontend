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

package controllers

import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.deregister_requirements

class DeregisterRequirementsControllerSpec extends ControllerSpecBase {

  lazy val SUT: DeregisterRequirementsController = inject[DeregisterRequirementsController]

  def viewAsString(): String = inject[deregister_requirements].apply(
    frontendAppConfig,
    continueUrl = s"http://localhost:8080/portal/vat-variations/org/$testVrn/introduction?lang=eng"
  )(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequest, messages).toString

  "DeregisterRequirements Controller" must {

    "return OK and the correct view for a GET" in {
      val result = SUT.onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }

}
