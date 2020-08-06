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

package connectors

import base.SpecBase
import connectors.payments.{NextUrl, PayConnector, StartPaymentJourneyBtaVat}
import models.Vrn
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global


// todo needs to be replaced with wiremock tests
class PayConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {

  private val testAmount = 1000
  private val testBackReturnUrl = "https://www.tax.service.gov.uk/business-account"
  private val testSpjRequest = StartPaymentJourneyBtaVat(testAmount, testBackReturnUrl, testBackReturnUrl, "123456789")

  def payConnector: PayConnector = inject[PayConnector]

  override final implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(bind[HttpClient].toInstance(mockHttpClient))
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vrn: Vrn = Vrn("vrn")

  "VatConnector" when {
    "vatPayLink is called" should {

      "return a NextUrl if the external service is responsive" in {
        val nextUrl = NextUrl("https://www.tax.service.gov.uk/pay/12345/choose-a-way-to-pay")

        mockPost(specificUrl = None)(mockedResponse = HttpResponse(CREATED, Some(Json.toJson(nextUrl))))

        val response = payConnector.vatPayLink(testSpjRequest)

        whenReady(response) { r =>
          r mustBe nextUrl
        }
      }

      "return the service-unavailable page if there is a problem" in {
        val nextUrl = NextUrl("http://localhost:9050/pay-online/service-unavailable")

        mockPost(specificUrl = None)(mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None))

        val response = payConnector.vatPayLink(testSpjRequest)

        whenReady(response) { r =>
          r mustBe nextUrl
        }
      }

    }
  }

}
