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

package controllers.vat

import controllers.ControllerSpecBase
import models.{VatDecEnrolment, Vrn}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import views.html.partials.vat.vat_enrol_address_tab

class VatEnrolAddressTabControllerSpec extends ControllerSpecBase with MockitoSugar {

  lazy val SUT: VatEnrolAddressTabController = inject[VatEnrolAddressTabController]

  val vatDecEnrolment: VatDecEnrolment = VatDecEnrolment(Vrn("a-users-vrn"), isActivated = true)

  def viewAsString(): String = vat_enrol_address_tab(emacUrlBuilder, vatDecEnrolment)(fakeRequest, messages).toString

  "VatEnrolAddressTabController" must {
    "return the correct view onPageLoad when" in {
      val result = SUT.onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
  
}
