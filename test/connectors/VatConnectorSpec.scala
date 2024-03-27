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

package connectors

import base.SpecBase
import models.requests.AuthenticatedRequest
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.http.Status._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate

// todo needs to be replaced with wiremock tests
class VatConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {

  override final implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(bind[HttpClient].toInstance(mockHttpClient))
      .build()

  lazy val SUT: VatConnector = inject[VatConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val request: Request[_] = Request(
    AuthenticatedRequest(fakeRequest, "", VatDecEnrolment(Vrn(""), isActivated = true), vatVarEnrolment = VatVarEnrolment(Vrn(""), isActivated = true), credId = ""),
    HtmlFormat.empty
  )

  val vrn: Vrn = Vrn("vrn")

  "VatConnector" when {

    "accountSummary is called" should {

      "return valid AccountSummaryData" in {
        val vatAccountSummary: AccountSummaryData = AccountSummaryData(Some(AccountBalance(Some(4.0))), None)

        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(OK, Json.toJson(vatAccountSummary), Map.empty[String, Seq[String]]))

        val response = SUT.accountSummary(vrn)

        whenReady(response) { r =>
          r mustBe Some(vatAccountSummary)
        }
      }

      "return 404 if nothing is returned" in {
        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(NOT_FOUND, None.toString))

        val response = SUT.accountSummary(vrn)

        whenReady(response) { r =>
          r mustBe None
        }
      }

      "return MicroServiceException if response couldn't be mapped" in {
        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, None.toString))

        val response = SUT.accountSummary(vrn)

        whenReady(response) { mse =>
          mse mustBe None
          verifyGet(url = "http://localhost:8880/vat/vrn/accountSummary")(wanted = 1)
        }
      }

      "return MicroServiceException if response is FORBIDDEN" in {
        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(FORBIDDEN, None.toString))

        val response = SUT.accountSummary(vrn)

        whenReady(response) { mse =>
          mse mustBe None
          verifyGet(url = "http://localhost:8880/vat/vrn/accountSummary")(wanted = 1)
        }
      }
    }

    "calender is called" should {

      "return valid calenderData" in {
        val vatCalender = CalendarData(
          Some("0000"), DirectDebit(true, None), None, Seq(CalendarPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2019-04-02"), None, true))
        )

        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(OK, Json.toJson(vatCalender), Map.empty[String, Seq[String]]))

        val response = SUT.calendar(vrn)

        whenReady(response) {
          r =>
            r mustBe Some(vatCalender)
        }
      }

      "return 404 if nothing is returned" in {
        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(NOT_FOUND, None.toString))

        val response = SUT.calendar(vrn)

        whenReady(response) { r =>
          r mustBe None
        }
      }

      "return MicroServiceException if response couldn't be mapped" in {
        mockGet(specificUrl = None)(mockedResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, None.toString))

        val response = SUT.calendar(vrn)

        whenReady(response) { mse =>
          mse mustBe None
          verifyGet(url = "http://localhost:8880/vat/vrn/calendar")(wanted = 1)
        }
      }
    }
  }

}
