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
import _root_.models.{UserEnrolments, UserEnrolmentStatus}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class EnrolmentStoreConnectorSpec  extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {
  implicit val hc = HeaderCarrier()

  val httpGet = mock[HttpClient]

  val connector = new EnrolmentStoreConnectorImpl(httpGet,frontendAppConfig)
  def result = connector.getEnrolments("cred-id")

  "EnrolmentStoreConnectorSpec" when {
    "getEnrolments is called" should {
      "handle a 200 response with a single enrolment" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(200, Some(Json.parse(
            """
              |{
              |"enrolments":[
              |{
              |"service":"IR-PAYE",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13T17:36:00.000Z"
              |}]
              |}
            """.stripMargin))))
        )
        result.futureValue mustBe  Right(UserEnrolments(List(UserEnrolmentStatus("IR-PAYE", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000Z").toLocalDateTime)))))
      }
      "handle a 200 response with multiple enrolments" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(200, Some(Json.parse(
            """
              |{
              |"enrolments":[
              |{
              |"service":"IR-PAYE",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13T17:36:00.000Z"
              |},
              |{"service":"VAT",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13T17:36:00.000Z"
              |},
              |{"service":"SA",
              |"state":"active",
              |"enrolmentTokenExpiryDate":"2018-10-13T17:36:00.000Z"
              |}
              |]
              |}
            """.stripMargin))))
        )
        result.futureValue mustBe Right(UserEnrolments(List(UserEnrolmentStatus("IR-PAYE", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000Z").toLocalDateTime)),
          UserEnrolmentStatus("VAT", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000Z").toLocalDateTime)),
          UserEnrolmentStatus("SA", Some("active"), Some(new DateTime("2018-10-13T17:36:00.000Z").toLocalDateTime)))))
      }
      "handle a 200 response with invalid JSON" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(200, Some(Json.parse(
            """
              |{
              |"enrolments":""
              |}
            """.stripMargin))))
        )
        result.futureValue mustBe Left("Unable to parse data from enrolment API")
      }
      "handle a 404 response" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(404, None))
        )
        result.futureValue mustBe Left("User not found from enrolment API")
      }
      "handle a 400 response" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(400, None))
        )
        result.futureValue mustBe Left("Bad request to enrolment API")
      }
      "handle a 403 response" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(403, None))
        )
        result.futureValue mustBe Left("Forbidden from enrolment API")
      }
      "handle a 503 response" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(503, None))
        )
        result.futureValue mustBe Left("Unexpected error from enrolment API")
      }
      "handle a 204 response" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(204, None))
        )
        result.futureValue mustBe Left("No content from enrolment API")
      }
      "handle a failed response from server" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.failed(Upstream5xxResponse("", 500, 500))
        )
        result.futureValue mustBe Left("Exception thrown from enrolment API")
      }
      "handle an incorrect code response" in {

        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse(823, None))
        )
        result.futureValue mustBe Left("Enrolment API couldn't handle response code")
      }
    }
  }
}
