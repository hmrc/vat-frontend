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

import forms.VatNotAddedForm
import models.VatNotAddedFormModel
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import views.html.{unauthorised, whichAccountAddVat}

import scala.concurrent.Future

class UnauthorisedControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures {

  implicit val request: Request[AnyContent] = fakeRequest

  lazy val vatNotAddedForm: VatNotAddedForm = inject[VatNotAddedForm]
  lazy val form: Form[VatNotAddedFormModel] = vatNotAddedForm.form
  lazy val SUT: UnauthorisedController = inject[UnauthorisedController]

  "Calling UnauthοrisedController.onPageLoad" must {
    "return 401 for a GET" in {
      val result: Future[Result] = SUT.onPageLoad()(fakeRequest)
      status(result) mustBe UNAUTHORIZED
    }

    "return the correct view for a GET" in {
      val result: Future[Result] = SUT.onPageLoad()(fakeRequest)
      contentAsString(result) mustBe inject[unauthorised].apply(frontendAppConfig)(fakeRequest, messages).toString
    }
  }

  "Calling UnauthοrisedController.continue" must {
    "return 200 for a GET" in {
      val result: Future[Result] = SUT.continue()(fakeRequest)
      status(result) mustBe OK
    }

    "return the correct view for a GET" in {
      val result: Future[Result] = SUT.continue()(fakeRequest)
      contentAsString(result) mustBe inject[whichAccountAddVat].apply(form, frontendAppConfig)(fakeRequest, messages).toString
    }
  }

  "Calling UnauthοrisedController.processForm" must {
    "redirect to the 'You already manage your taxes, duties and schemes online' page when 'sign_in_to_other_account' is selected" in {
      val validFormData: (String, String) = "radioOption" -> "sign_in_to_other_account"
      val result: Future[Result] = SUT.processForm()(fakeRequest.withFormUrlEncodedBody(validFormData))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe "http://localhost:9020/business-account/wrong-credentials"

    }

    "redirect to the 'Which VAT service do you want to add' page when 'add_vat_to_this_account' is selected" in {
      val validFormData: (String, String) = "radioOption" -> "add_vat_to_this_account"
      val result: Future[Result] = SUT.processForm()(fakeRequest.withFormUrlEncodedBody(validFormData))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe "http://localhost:9730/business-account/add-tax/vat"
    }

    "display the 'Which Account to Add VAT' page with the form displaying errors when form submitted without data (no selection)" in {
      val result: Future[Result] = SUT.processForm(fakeRequest.withFormUrlEncodedBody())

      status(result) mustBe BAD_REQUEST
    }

    "return a Bad Request when form submitted with invalid data" in {
      val invalidFormData: (String, String) = "radioOption" -> "this_no_good_option"
      val result: Future[Result] = SUT.processForm()(fakeRequest.withFormUrlEncodedBody(invalidFormData))

      status(result) mustBe BAD_REQUEST
    }
  }

}
