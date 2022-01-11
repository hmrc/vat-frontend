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

package utils

import base.SpecBase
import config.FrontendAppConfig
import models.{VatDecEnrolment, VatVarEnrolment, Vrn}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock

class EmacUrlBuilderSpec extends SpecBase with BeforeAndAfterEach {

  val vatDecEnrolment: VatDecEnrolment = VatDecEnrolment(Vrn("a-users-vrn"), isActivated = true)
  val vatVarEnrolment: VatVarEnrolment = VatVarEnrolment(Vrn("vrn"), isActivated = true)
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    reset(mockAppConfig)
  }

  override def emacUrlBuilder = new EmacUrlBuilder(mockAppConfig)

  "build EMAC enrolment url" when {
    "emac enrolment url feature is true" should {
      "return EMAC URL" in {
        when(mockAppConfig.emacVatEnrolmentUrl)
          .thenReturn("trueUrl")

        emacUrlBuilder.getEnrolmentUrl("vat-change-details-enrol")(Some(vatDecEnrolment)) mustBe
          "trueUrl"
      }
    }
  }

  "build EMAC activation url" when {

    "emac activation url feature is true" should {
      "return Emac URL" in {
        when(mockAppConfig.emacVatActivationUrl)
          .thenReturn("trueUrl")

        emacUrlBuilder.getActivationUrl("vat-change-details")(Some(vatDecEnrolment)) mustBe
          "trueUrl"
      }
    }
  }

  "build EMAC lost pin url" when {
    "emac activation url feature is true" should {
      "return emac url" in {
        when(mockAppConfig.emacVatLostPinUrl)
          .thenReturn("trueUrl")

        emacUrlBuilder.getLostPinUrl mustBe Some("trueUrl")
      }
    }
  }
}
