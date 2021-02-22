/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import _root_.models.Eligibility
import base.SpecBase
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class VatDeferralNewPaymentSchemeConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {

  implicit val hc = HeaderCarrier()

  val httpGet = mock[HttpClient]

  val connector = new VatDeferralNewPaymentSchemeConnector(httpGet, frontendAppConfig)
  val testVrn = "12345678"

  def result = connector.eligibility(testVrn)

  "VatDeferralNewPaymentSchemeConnector" when {
    "eligibility is called" should {
      "return true when a user already has a payment plan setup" in {
        when(httpGet.GET[Eligibility](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(Eligibility(Some(true), Some(false), Some(false),Some(false),Some(false))))

        result.futureValue mustBe Some(true)
      }

      "return false when a user isnt eligible for a payment plan" in {
        when(httpGet.GET[Eligibility](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(Eligibility(Some(false), Some(false), Some(false),Some(false),Some(true))))

        result.futureValue mustBe Some(false)
      }

      "return None when a user is eligible has doesnt have payment plan setup with existing obligations," +
        "payment on account and time to pay exists" in {
        when(httpGet.GET[Eligibility](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(Eligibility(Some(false), Some(true), Some(true),Some(true),Some(false))))

        result.futureValue mustBe None
      }

      "return None when a user is eligible has doesnt have payment plan setup" in {
        when(httpGet.GET[Eligibility](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(Eligibility(Some(false), Some(false), Some(false),Some(false),Some(false))))

        result.futureValue mustBe None
      }
    }
  }

}
