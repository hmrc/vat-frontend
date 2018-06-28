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

package utils

import base.SpecBase
import config.FrontendAppConfig
import models.{VatDecEnrolment, VatVarEnrolment}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn

class EmacUrlBuilderSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val vatDecEnrolment = VatDecEnrolment(Vrn("a-users-vrn"), isActivated = true)
  val vatVarEnrolment = VatVarEnrolment(Vrn("vrn"), isActivated = true)
  val mockAppConfig = mock[FrontendAppConfig]

  override def beforeEach() = {
    reset(mockAppConfig)
  }

  override def emacUrlBuilder = new EmacUrlBuilder(mockAppConfig)

  "build EMAC enrolment url" when {
    "emac enrolment url feature is true" should {
      "return EMAC URL" in {
        when(mockAppConfig.useEmacVatEnrolment).thenReturn(true)
        when(mockAppConfig.emacVatEnrolmentUrl).thenReturn(
          "trueUrl")

        emacUrlBuilder.getEnrolmentUrl("vat-change-details-enrol")(Some(vatDecEnrolment))(fakeRequest) mustBe
          "trueUrl"
      }
    }

    "emac enrolment url feature is false" should {
      "not return EMAC URL" in {

        when(mockAppConfig.useEmacVatEnrolment).thenReturn(false)
        when(mockAppConfig.getPortalUrl("vat-change-details-enrol")(Some(vatDecEnrolment))(fakeRequest)).thenReturn(
          "falseUrl")

        emacUrlBuilder.getEnrolmentUrl("vat-change-details-enrol")(Some(vatDecEnrolment))(fakeRequest) mustBe
          "falseUrl"
      }
    }
  }

  "build EMAC activation url" when {

    "emac activation url feature is true" should {
      "return Emac URL" in {
        when(mockAppConfig.useEmacVatActivation).thenReturn(true)
        when(mockAppConfig.emacVatActivationUrl).thenReturn(
          "trueUrl")

        emacUrlBuilder.getActivationUrl("vat-change-details")(Some(vatDecEnrolment))(fakeRequest) mustBe
          "trueUrl"
      }
    }

    "emac activation url feature is false" should {
      "return Emac URL" in {
        when(mockAppConfig.useEmacVatActivation).thenReturn(false)
        when(mockAppConfig.getPortalUrl("vat-change-details")(Some(vatDecEnrolment))(fakeRequest)).thenReturn(
          "falseUrl")

        emacUrlBuilder.getActivationUrl("vat-change-details")(Some(vatDecEnrolment))(fakeRequest) mustBe
          "falseUrl"
      }
    }
  }

}
