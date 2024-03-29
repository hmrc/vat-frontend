/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ThresholdService
import views.html.deregister

class DeregisterControllerSpec extends ControllerSpecBase {

  lazy val SUT: DeregisterController = inject[DeregisterController]
  lazy val thresholdService: ThresholdService = inject[ThresholdService]
  implicit val request: Request[_] = FakeRequest()
  def viewAsString(): String = inject[deregister].apply(frontendAppConfig, thresholdService.formattedVatThreshold())(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequest, messages).toString

  "Deregister Controller" must {

    "return OK and the correct view for a GET" in {
      val result = SUT.onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }

}




