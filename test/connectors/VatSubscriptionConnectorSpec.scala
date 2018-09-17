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

package connectors

import base.SpecBase
import connectors.models._
import org.mockito.Matchers
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class VatSubscriptionConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {

  def vatSubscriptionConnector[A](mockedResponse: HttpResponse, httpWrapper: HttpWrapper = mock[HttpWrapper]): VatSubscriptionConnector = {
    when(httpWrapper.getF[A](Matchers.any())).
        thenReturn(mockedResponse)
    new VatSubscriptionConnector(http(httpWrapper), frontendAppConfig)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vrn = Vrn("vrn")

  "VatSubscriptionConnector" when {

    "mandationStatus is called" should {

      "return a valid MandationStatus" in {

        val vatMandationStatus = MandationStatus("MTDfB Mandated")

        val response = vatSubscriptionConnector(
          mockedResponse = HttpResponse(OK, Some(Json.toJson(vatMandationStatus)
          ))).mandationStatus(vrn)

        whenReady(response) { res =>
          res mustBe Some(vatMandationStatus)
        }
      }

      "return None if nothing is returned" in {
        val response = vatSubscriptionConnector(
          mockedResponse = HttpResponse(NOT_FOUND, None)
        ).mandationStatus(vrn)

        whenReady(response) { res =>
          res mustBe None
        }
      }

      "throw a MicroServiceException if response couldn't be mapped" in {
        val vatSubscriptionUri: String = "http://localhost:8080/vat-subscription/vrn/customer-details"
        val httpWrapper = mock[HttpWrapper]

        val response = vatSubscriptionConnector(
          mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
          httpWrapper
        ).mandationStatus(vrn)

        whenReady(response.failed) { mse =>
          mse mustBe a[MicroServiceException]
          verify(httpWrapper).getF[MandationStatus](vatSubscriptionUri)
        }
      }
    }
  }
}
