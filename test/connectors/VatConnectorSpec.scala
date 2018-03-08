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
import connectors.models.{AccountBalance, AccountSummaryData, _}
import org.mockito.Matchers
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{CtUtr, Vrn}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.util.Failure

class VatConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {

  def vatConnector[A](mockedResponse: HttpResponse, httpWrapper: HttpWrapper = mock[HttpWrapper]): VatConnector = {
    when(httpWrapper.getF[A](Matchers.any())).
        thenReturn(mockedResponse)
    new VatConnector(http(httpWrapper), frontendAppConfig)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vrn = Vrn("vrn")

  "VatConnector account summary" should {

    "call the micro service with the correct uri and return the contents" in {
      val vatAccountSummary = AccountSummaryData(Some(AccountBalance(Some(4.0))), None)

      val response = vatConnector(
          mockedResponse = HttpResponse(OK, Some(Json.toJson(vatAccountSummary)
        ))).accountSummary(vrn)

      whenReady(response) { r =>
        r mustBe Some(vatAccountSummary)
      }
    }

    "call the micro service with the correct uri and return no contents if there are none" in {
      val response = vatConnector(
        mockedResponse = HttpResponse(NOT_FOUND, None)
      ).accountSummary(vrn)

      whenReady(response) { r =>
        r mustBe None
      }
    }

    "call the micro service and return 500" in {
      val vatAccountSummaryUri = "http://localhost:8880/vat/vrn/account-summary" // TODO: get correct url
      val httpWrapper = mock[HttpWrapper]

      val response = vatConnector(
        mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
        httpWrapper
      ).accountSummary(vrn)

      whenReady(response.failed) { mse =>
        mse mustBe a[MicroServiceException]
        verify(httpWrapper).getF[AccountSummaryData](vatAccountSummaryUri)
      }
    }
  }

  "VatConnector designatory details" should {

    "Return the correct response for an example with designatory details information" in {

      val designatoryDetailsCollection = DesignatoryDetailsCollection(
        Some(DesignatoryDetails(DesignatoryDetailsName(nameLine1 = Some("name1"), nameLine2 = Some("name2"))))
      )

      val response = vatConnector(
        mockedResponse = HttpResponse(OK, Some(Json.toJson(designatoryDetailsCollection)))
      ).designatoryDetails(vrn)

      whenReady(response) {
        r =>
          r mustBe Some(designatoryDetailsCollection)
      }
    }

    "call the micro service and return 500" in {


    }

  }


}
