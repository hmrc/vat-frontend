/*
 * Copyright 2019 HM Revenue & Customs
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
import connectors.payments.{NextUrl, PayConnector, SpjRequestBtaVat, VatPeriod}
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class PayConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {

  private val testVatPeriod = VatPeriod(10, 10)
  private val testAmount = 1000
  private val testBackReturnUrl = "https://www.tax.service.gov.uk/business-account"
  private val testSpjRequest = SpjRequestBtaVat(testAmount, testBackReturnUrl, testBackReturnUrl, testVatPeriod, "123456789")

  def payConnector[A](mockedResponse: HttpResponse, httpWrapper: HttpWrapper = mock[HttpWrapper]): PayConnector = {
    when(httpWrapper.postF[A](Matchers.any())).
        thenReturn(mockedResponse)
    new PayConnector(http(httpWrapper), frontendAppConfig)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vrn = Vrn("vrn")

  "VatConnector" when {

    "vatPayLink is called" should {

      "return a NextUrl if the external service is responsive" in {
        val nextUrl = NextUrl("https://www.tax.service.gov.uk/pay/12345/choose-a-way-to-pay")

        val response = payConnector(
          mockedResponse = HttpResponse(CREATED, Some(Json.toJson(nextUrl)
          ))).vatPayLink(testSpjRequest)

        whenReady(response) { r =>
          r mustBe nextUrl
        }
      }

      "return the service-unavailable page if there is a problem" in {
        val nextUrl = NextUrl("http://localhost:9050/pay-online/service-unavailable")

        val response = payConnector(
          mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None)
        ).vatPayLink(testSpjRequest)

        whenReady(response) { r =>
          r mustBe nextUrl
        }
      }
    }

//    "designatoryDetails is called" should {
//
//      "return valid DesignatoryDetailsCollection" in {
//
//        val designatoryDetailsCollection = DesignatoryDetailsCollection(
//          Some(DesignatoryDetails(DesignatoryDetailsName(nameLine1 = Some("name1"), nameLine2 = Some("name2"))))
//        )
//
//        val response = vatConnector(
//          mockedResponse = HttpResponse(OK, Some(Json.toJson(designatoryDetailsCollection)))
//        ).designatoryDetails(vrn)
//
//        whenReady(response) {
//          r =>
//            r mustBe Some(designatoryDetailsCollection)
//        }
//      }
//
//      "return 404 if nothing is returned" in {
//        val response = vatConnector(
//          mockedResponse = HttpResponse(NOT_FOUND, None)
//        ).designatoryDetails(vrn)
//
//        whenReady(response) { r =>
//          r mustBe None
//        }
//      }
//
//      "return MicroServiceException if response couldn't be mapped" in {
//        val vatAccountSummaryUri = "http://localhost:8880/vat/vrn/designatoryDetails"
//        val httpWrapper = mock[HttpWrapper]
//
//        val response = vatConnector(
//          mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
//          httpWrapper
//        ).designatoryDetails(vrn)
//
//        whenReady(response.failed) { mse =>
//          mse mustBe a[MicroServiceException]
//          verify(httpWrapper).getF[AccountSummaryData](vatAccountSummaryUri)
//        }
//
//      }
//
//    }

//    "calender is called" should {
//
//      "return valid calenderData" in {
//        val vatCalender = CalendarData(
//          Some("0000"), DirectDebit(true, None), None, Seq(CalendarPeriod(new LocalDate("2018-04-02"), new LocalDate("2019-04-02"), None, true))
//        )
//        val response = vatConnector(
//          mockedResponse = HttpResponse(OK, Some(Json.toJson(vatCalender)))).calendar(vrn)
//
//        whenReady(response) {
//          r =>
//            r mustBe Some(vatCalender)
//        }
//      }
//
//      "return 404 if nothing is returned" in {
//        val response = vatConnector(
//          mockedResponse = HttpResponse(NOT_FOUND, None)
//        ).calendar(vrn)
//
//        whenReady(response) { r =>
//          r mustBe None
//        }
//      }
//
//      "return MicroServiceException if response couldn't be mapped" in {
//        val vatCalendarUri = "http://localhost:8880/vat/vrn/calendar"
//        val httpWrapper = mock[HttpWrapper]
//
//        val response = vatConnector(
//          mockedResponse = HttpResponse(INTERNAL_SERVER_ERROR, None),
//          httpWrapper
//        ).calendar(vrn)
//
//        whenReady(response.failed) { mse =>
//          mse mustBe a[MicroServiceException]
//          verify(httpWrapper).getF[CalendarData](vatCalendarUri)
//        }
//      }
//    }
  }
}
