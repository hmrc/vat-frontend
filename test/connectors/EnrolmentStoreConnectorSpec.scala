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

import _root_.models.{UserEnrolmentStatus, UserEnrolments}
import base.SpecBase
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// todo needs to be replaced with wiremock tests
class EnrolmentStoreConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {
  implicit val hc = HeaderCarrier()

  val httpGet = mock[HttpClient]

  val connector = new EnrolmentStoreConnectorImpl(httpGet, frontendAppConfig)

  def result = connector.getEnrolments("cred-id")

  "EnrolmentStoreConnectorSpec" when {
    "getEnrolments is called" should {
      "handle a 200 response with a single enrolment" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(status = 200, json = Json.parse(
            """
              |{
              |"enrolments":[
              |{
              |"service":"IR-PAYE",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13 17:36:00.000"
              |}]
              |}
            """.stripMargin), headers = Map.empty[String, Seq[String]]))
        )
        result.futureValue mustBe Right(UserEnrolments(List(UserEnrolmentStatus("IR-PAYE", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000").toLocalDateTime)))))
      }
      "handle a 200 response with multiple enrolments" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(status = 200, json = Json.parse(
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
        result.futureValue mustBe Right(UserEnrolments(List(UserEnrolmentStatus("IR-PAYE", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000").toLocalDateTime)),
          UserEnrolmentStatus("VAT", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000").toLocalDateTime)),
          UserEnrolmentStatus("SA", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000").toLocalDateTime)))))
      }
      "handle a 200 response with invalid JSON" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse(status = 200, json = Json.parse(
            """
              |{
              |"enrolments":""
              |}
            """.stripMargin), headers = Map.empty[String, Seq[String]]))
        )
        result.futureValue mustBe Left("Unable to parse data from enrolment API")
      }
      "handle a 404 response" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(404, None.toString))
        )
        result.futureValue mustBe Left("User not found from enrolment API")
      }
      "handle a 400 response" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Bad request to enrolment API", 400, 400))
        )
        result.futureValue mustBe Left("Bad request to enrolment API")
      }
      "handle a 403 response" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Forbidden from enrolment API", 403, 403))
        )
        result.futureValue mustBe Left("Forbidden from enrolment API")
      }
      "handle a 503 response" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Unexpected error from enrolment API", 503, 503))
        )
        result.futureValue mustBe Left("Unexpected error from enrolment API")
      }
      "handle a 204 response" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(204, None.toString))
        )
        result.futureValue mustBe Left("No content from enrolment API")
      }
      "handle a failed response from server" in {
        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse.apply("Exception thrown from enrolment API", 500, 500))
        )
        result.futureValue mustBe Left("Exception thrown from enrolment API")
      }
      "handle an incorrect code response" in {

        when(httpGet.GET[HttpResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(HttpResponse.apply(823, None.toString))
        )
        result.futureValue mustBe Left("Enrolment API couldn't handle response code")
      }
    }
  }
}
