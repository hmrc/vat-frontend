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
import connectors.models.designatorydetails.{DesignatoryDetails, DesignatoryDetailsCollection, DesignatoryDetailsName}
import connectors.models.{AccountBalance, AccountSummaryData, CalendarData, _}
import org.joda.time.LocalDate
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

  "VatConnector" when {

    "accountSummary is called" should {

      "return valid AccountSummaryData" in {
        val vatAccountSummary = AccountSummaryData(Some(AccountBalance(Some(4.0))), None)

        val response = vatConnector(
          mockedResponse = HttpResponse(OK, Some(Json.toJson(vatAccountSummary)
          ))).accountSummary(vrn)

        whenReady(response) { r =>
          r mustBe Some(vatAccountSummary)
        }
      }

      "return 404 if nothing is returned" in {
        val response = vatConnector(
          mockedResponse = HttpResponse(NOT_FOUND, None)
        ).accountSummary(vrn)

        whenReady(response) { r =>
          r mustBe None
        }
      }

      "return MicroServiceException if response couldn't be mapped" in {
        val vatAccountSummaryUri = "http://localhost:8880/vat/vrn/accountSummary"
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

      "return MicroServiceException if response is FORBIDDEN" in {
        val vatAccountSummaryUri = "http://localhost:8880/vat/vrn/accountSummary"
        val httpWrapper = mock[HttpWrapper]

        val response = vatConnector(
          mockedResponse = HttpResponse(FORBIDDEN, None),
          httpWrapper
        ).accountSummary(vrn)

        whenReady(response.failed) { mse =>
          mse mustBe a[MicroServiceException]
          verify(httpWrapper).getF[AccountSummaryData](vatAccountSummaryUri)
        }
      }
    }

    "designatoryDetails is called" should {

      "return valid DesignatoryDetailsCollection" in {

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

      "return 404 if nothing is returned" in {
        val response = vatConnector(
          mockedResponse = HttpResponse(NOT_FOUND, None)
        ).designatoryDetails(vrn)

        whenReady(response) { r =>
          r mustBe None
        }
      }

      "return MicroServiceException if response couldn't be mapped" in {
        val vatAccountSummaryUri = "http://localhost:8880/vat/vrn/designatoryDetails"
        val httpWrapper = mock[HttpWrapper]

        val response = vatConnector(
          mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
          httpWrapper
        ).designatoryDetails(vrn)

        whenReady(response.failed) { mse =>
          mse mustBe a[MicroServiceException]
          verify(httpWrapper).getF[AccountSummaryData](vatAccountSummaryUri)
        }

      }

    }

    "calender is called" should {

      "return valid calenderData" in {
        val vatCalender = CalendarData(
          Some("0000"), DirectDebit(true, None), None, Seq(CalendarPeriod(new LocalDate("2018-04-02"), new LocalDate("2019-04-02"), None, true))
        )
        val response = vatConnector(
          mockedResponse = HttpResponse(OK, Some(Json.toJson(vatCalender)))).calendar(vrn)

        whenReady(response) {
          r =>
            r mustBe Some(vatCalender)
        }
      }

      "return 404 if nothing is returned" in {
        val response = vatConnector(
          mockedResponse = HttpResponse(NOT_FOUND, None)
        ).calendar(vrn)

        whenReady(response) { r =>
          r mustBe None
        }
      }

      "return MicroServiceException if response couldn't be mapped" in {
        val vatCalendarUri = "http://localhost:8880/vat/vrn/calendar"
        val httpWrapper = mock[HttpWrapper]

        val response = vatConnector(
          mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
          httpWrapper
        ).calendar(vrn)

        whenReady(response.failed) { mse =>
          mse mustBe a[MicroServiceException]
          verify(httpWrapper).getF[CalendarData](vatCalendarUri)
        }
      }
    }
  }
}
