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

import forms.VatNotAddedForm
import models.VatNotAddedFormModel
import play.api.test.Helpers._
import views.html.unauthorised
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.data.Forms._

class UnauthorisedControllerSpec extends ControllerSpecBase with MockitoSugar {

  trait LocalSetup {
    lazy val vatNotAddedForm: VatNotAddedForm = injector.instanceOf[VatNotAddedForm]
    lazy val form: Form[VatNotAddedFormModel] = vatNotAddedForm.form
    lazy val validData: Map[String, String] = Map(
      "value" -> VatNotAddedFormModel.options.head.value
    )
  }

  "Unauthorised Controller" must {
    "return 401 for a GET" in new LocalSetup {
      val result = new UnauthorisedController(frontendAppConfig, messagesApi, vatNotAddedForm).onPageLoad()(fakeRequest)
      status(result) mustBe UNAUTHORIZED
    }

    "return the correct view for a GET" in new LocalSetup  {
      val result = new UnauthorisedController(frontendAppConfig, messagesApi, vatNotAddedForm).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe unauthorised(form, frontendAppConfig)(fakeRequest, messages).toString
    }

  }

  "Calling UnauthοrisedController.processForm" must {
    "redirect to the 'You already manage your taxes, duties and schemes online' page when the option 'sign_in_to_other_account' is selected" in new LocalSetup  {

    }

    "redirect to the 'Which VAT service do you want to add' page when the option 'add_your_vat_to_this_account' is selected" ignore new LocalSetup  {

    }

    "display the 'unauthorised' page with the form displaying errors when submitted without any selection" in new LocalSetup  {

    }

  }
}
