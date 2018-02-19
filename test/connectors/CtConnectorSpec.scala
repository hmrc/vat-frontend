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
import uk.gov.hmrc.domain.CtUtr
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class CtConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {
  

  def ctConnector[A](mockedResponse: HttpResponse, httpWrapper: HttpWrapper = mock[HttpWrapper]): CtConnector = {
    when(httpWrapper.getF[A](Matchers.any())).
      thenReturn(mockedResponse)
    new CtConnector(http(httpWrapper), frontendAppConfig)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val ctUtr = CtUtr("utr")

  "CtConnector account summary" should {

    "call the micro service with the correct uri and return the contents" in {

      val ctAccountSummary = CtAccountSummaryData(Some(CtAccountBalance(Some(4.0))))

      val response = ctConnector(
        mockedResponse = HttpResponse(OK, Some(Json.toJson(ctAccountSummary)
        ))).accountSummary(ctUtr)

      whenReady(response) { r =>
        r mustBe Some(ctAccountSummary)
      }

    }

    "call the micro service with the correct uri and return no contents if there are none" in {

      val response = ctConnector(
        mockedResponse = HttpResponse(NOT_FOUND, None)
      ).accountSummary(ctUtr)

      whenReady(response) { r =>
        r mustBe None
      }
    }

    "call the micro service and return 500" in {
      val ctAccountSummaryUri = "http://localhost:8647/ct/utr/account-summary"
      val httpWrapper = mock[HttpWrapper]

      val response = ctConnector(
        mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
        httpWrapper
      ).accountSummary(ctUtr)

      whenReady(response.failed) { mse =>
        mse mustBe a[MicroServiceException]
        verify(httpWrapper).getF[CtAccountSummaryData](ctAccountSummaryUri)
      }

    }
  }

  val sampleDesignatoryDetails =
    """{
      |  "company": {
      |    "name": {
      |      "nameLine1": "A B",
      |      "nameLine2": "xyz"
      |    },
      |    "address": {
      |      "addressLine1": "1 Fake Street",
      |      "addressLine2": "X",
      |      "addressLine3": "Nowhere",
      |      "addressLine4": "Nowhere",
      |      "addressLine5": "Nowhere",
      |      "postcode": "AA00 0AA",
      |      "foreignCountry": "France"
      |    }
      |  },
      |  "communication": {
      |    "name": {
      |      "nameLine1": "A B",
      |      "nameLine2": "xyz"
      |    },
      |    "address": {
      |      "addressLine1": "1 Fake Street",
      |      "addressLine2": "X",
      |      "addressLine3": "Nowhere",
      |      "addressLine4": "Nowhere",
      |      "addressLine5": "Nowhere",
      |      "postcode": "AA00 0AA",
      |      "foreignCountry": "France"
      |    }
      |  }
      |}""".stripMargin

  "CtConnector designatory details" should {

    "Return the correct response for an example with designatory details information" in {

      val response = ctConnector(
        mockedResponse = HttpResponse(OK, Some(Json.parse(sampleDesignatoryDetails)))
      ).designatoryDetails(ctUtr)

      val expectedDetails = Some(DesignatoryDetails(
        DesignatoryDetailsName(Some("A B"), Some("xyz")),
        Some(DesignatoryDetailsAddress(
          Some("1 Fake Street"),
          Some("X"),
          Some("Nowhere"),
          Some("Nowhere"),
          Some("Nowhere"),
          Some("AA00 0AA"),
          Some("France")
        ))
      ))

      val expected = CtDesignatoryDetailsCollection(expectedDetails, expectedDetails)

      whenReady(response) { r =>
        r mustBe Some(expected)
      }
    }

    "call the micro service and return 500" in {

      val response = ctConnector(
        mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None)
      ).designatoryDetails(ctUtr)

      whenReady(response.failed) { mse =>
        mse mustBe a[MicroServiceException]
      }
    }

  }


}
