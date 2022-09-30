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

package connectors

import _root_.models.{UserEnrolmentStatus, UserEnrolments, VatDecEnrolment, VatVarEnrolment, Vrn}
import base.SpecBase
import models.requests.AuthenticatedRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// todo needs to be replaced with wiremock tests
class EnrolmentStoreConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  
  implicit val request: Request[_] = Request(
    AuthenticatedRequest(fakeRequest, "", VatDecEnrolment(Vrn(""), isActivated = true), vatVarEnrolment = VatVarEnrolment(Vrn(""), isActivated = true), credId = ""),
    HtmlFormat.empty
  )

  val http: HttpClient = mock[HttpClient]

  val connector = new EnrolmentStoreConnectorImpl(http, frontendAppConfig)

  val invalidErrorCode: Int = 823

  def result: Future[Either[String, UserEnrolments]] = connector.getEnrolments("cred-id")

  "EnrolmentStoreConnectorSpec" when {
    "getEnrolments is called" should {
      "handle a 200 response with a single enrolment" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(HttpResponse(status = OK, json = Json.parse(
            """
              |{
              | "enrolments":[
              |   {
              |    "service":"IR-PAYE",
              |    "state":"active",
              |    "enrolmentTokenExpiryDate":"2018-10-13 17:36:00.000"
              |   }
              | ]
              |}
            """.stripMargin), headers = Map.empty[String, Seq[String]]))
        )
        result.futureValue mustBe Right(UserEnrolments(List(UserEnrolmentStatus("IR-PAYE", Some("active"), Some(LocalDateTime.parse("2018-10-13T17:36:00.000"))))))
      }
      "handle a 200 response with multiple enrolments" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(status = OK, json = Json.parse(
            """
              |{
              |"enrolments":[
              |{
              |"service":"IR-PAYE",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13 17:36:00.000"
              |},
              |{"service":"VAT",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13 17:36:00.000"
              |},
              |{"service":"SA",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13 17:36:00.000"
              |}
              |]
              |}
            """.stripMargin), headers = Map.empty[String, Seq[String]]))
        )
        result.futureValue mustBe Right(UserEnrolments(List(UserEnrolmentStatus("IR-PAYE", Some("active"), Some(LocalDateTime.parse("2018-10-13T17:36:00.000"))),
          UserEnrolmentStatus("VAT", Some("active"), Some(LocalDateTime.parse("2018-10-13T17:36:00.000"))),
          UserEnrolmentStatus("SA", Some("active"), Some(LocalDateTime.parse("2018-10-13T17:36:00.000"))))))
      }
      "handle a 200 response with invalid JSON" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse(status = OK, json = Json.parse(
            """
              |{
              |"enrolments":""
              |}
            """.stripMargin), headers = Map.empty[String, Seq[String]]))
        )
        result.futureValue mustBe Left("Unable to parse data from enrolment API")
      }
      "handle a 404 response" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(NOT_FOUND, None.toString))
        )
        result.futureValue mustBe Left("User not found from enrolment API")
      }
      "handle a 400 response" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Bad request to enrolment API", BAD_REQUEST, BAD_REQUEST))
        )
        result.futureValue mustBe Left("Bad request to enrolment API")
      }
      "handle a 403 response" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Forbidden from enrolment API", FORBIDDEN, FORBIDDEN))
        )
        result.futureValue mustBe Left("Forbidden from enrolment API")
      }
      "handle a 503 response" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Unexpected error from enrolment API", SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE))
        )
        result.futureValue mustBe Left("Unexpected error from enrolment API")
      }
      "handle a 204 response" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(NO_CONTENT, None.toString))
        )
        result.futureValue mustBe Left("No content from enrolment API")
      }
      "handle a failed response from server" in {
        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Exception thrown from enrolment API", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
        )
        result.futureValue mustBe Left("Exception thrown from enrolment API")
      }
      "handle an incorrect code response" in {

        when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(invalidErrorCode, None.toString))
        )
        result.futureValue mustBe Left("Enrolment API couldn't handle response code")
      }
    }
  }
}
