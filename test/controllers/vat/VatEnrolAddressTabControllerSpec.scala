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

package controllers.vat

import controllers.ControllerSpecBase
import controllers.actions.FakeAuthAction
import models.VatDecEnrolment
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Vrn
import views.html.partials.vat.vat_enrol_address_tab

class VatEnrolAddressTabControllerSpec extends ControllerSpecBase with MockitoSugar {

  def controller() = new VatEnrolAddressTabController(messagesApi, FakeAuthAction, frontendAppConfig, emacUrlBuilder)

  val vatDecEnrolment = VatDecEnrolment(Vrn("a-users-vrn"), isActivated = true)

  def viewAsString() = vat_enrol_address_tab(emacUrlBuilder, vatDecEnrolment)(fakeRequest, messages).toString

  "VatEnrolAddressTabController" must {
    "return the correct view onPageLoad when" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}