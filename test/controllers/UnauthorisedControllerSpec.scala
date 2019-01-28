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

package controllers

import models.VatNotAddedFormModel
import play.api.test.Helpers._
import views.html.unauthorised
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.data.Forms._

class UnauthorisedControllerSpec extends ControllerSpecBase with MockitoSugar {

  "Unauthorised Controller" must {
    "return 401 for a GET" in {
      val result = new UnauthorisedController(frontendAppConfig, messagesApi).onPageLoad()(fakeRequest)
      status(result) mustBe UNAUTHORIZED
    }

    "return the correct view for a GET" in {
      val result = new UnauthorisedController(frontendAppConfig, messagesApi).onPageLoad()(fakeRequest)
      val fakeForm: Form[VatNotAddedFormModel] = mock[Form[VatNotAddedFormModel]]
      val someFormAlpha = new VatNotAddedFormModel("testValue")

      val someForm: Form[VatNotAddedFormModel] = Form(
        mapping(
          "radioOption" -> nonEmptyText
        )(VatNotAddedFormModel.apply)(VatNotAddedFormModel.unapply)
      )

      contentAsString(result) mustBe unauthorised(someForm, frontendAppConfig)(fakeRequest, messages).toString
    }
  }
}
