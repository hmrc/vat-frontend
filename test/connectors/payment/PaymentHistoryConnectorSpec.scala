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

package connectors.payment

import connectors.payments.PaymentHistoryConnector
import models.Vrn
import models.payment.PaymentStatus.{Invalid, Successful}
import models.payment._
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.Future

class PaymentHistoryConnectorSpec extends PaymentConnectorHelper with MockitoSugar {

  val httpGet: HttpClient = mock[HttpClient]

  val payConnector: PaymentHistoryConnector = new PaymentHistoryConnector(httpGet, frontendAppConfig)

  "PayConnector" when {
    "GET is called" should {
      "handle a valid 200 response with minimum data" in {
        when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
          Future.successful(HttpResponse.apply(OK, Json.parse(
            """
              |{
              |"searchScope": "bta",
              |"searchTag":"search-tag",
              |"payments": []
              |}
            """.stripMargin), Map.empty[String, Seq[String]]))
        )

        val result = payConnector.get(Vrn(""))

        result.futureValue.leftSide shouldBe Right(Nil)
      }
    }
  }

  "handle a valid 200 response with single payments record" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(OK, Json.parse(
        """
          |{
          |"searchScope": "bta",
          |"searchTag":"search-tag",
          |"payments": [
          | {
          |"reference" : "reference number",
          |"amountInPence" : 100,
          |"status": "Successful",
          |"createdOn": "data string",
          |"taxType" : "tax type"
          | }
          |]
          |}
        """.stripMargin), Map.empty[String, Seq[String]]))
    )

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe
      Right(List(VatPaymentRecord("reference number", 100, Successful, "data string", "tax type")))
  }

  "handle a valid 200 response with multiple payment records" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(OK, Json.parse(
        """
          |{
          |"searchScope": "bta",
          |"searchTag":"search-tag",
          |"payments": [
          | {
          |"reference" : "reference number",
          |"amountInPence" : 100,
          |"status": "Successful",
          |"createdOn": "data string",
          |"taxType" : "tax type"
          | },
          | {
          |"reference" : "reference number 2",
          |"amountInPence" : 2000000000,
          |"status": "Successful",
          |"createdOn": "data string",
          |"taxType" : "tax type"
          | }
          |]
          |}
        """.stripMargin), Map.empty[String, Seq[String]]))
    )

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe
      Right(List(
        VatPaymentRecord("reference number", 100, Successful, "data string", "tax type"),
        VatPaymentRecord("reference number 2", 2000000000, Successful, "data string", "tax type")))
  }

  "handle an invalid status response within payment records" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(OK, Json.parse(
        """
          |{
          |"searchScope": "bta",
          |"searchTag":"search-tag",
          |"payments": [
          | {
          |"reference" : "reference number",
          |"amountInPence" : 100,
          |"status": "not-supported",
          |"createdOn": "data string",
          |"taxType" : "tax type"
          | },
          | {
          |"reference" : "reference number 2",
          |"amountInPence" : 2000000000,
          |"status": "Successful",
          |"createdOn": "data string",
          |"taxType" : "tax type"
          | }
          |]
          |}
        """.stripMargin), Map.empty[String, Seq[String]]))
    )

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe
      Right(List(
        VatPaymentRecord("reference number", 100, Invalid, "data string", "tax type"),
        VatPaymentRecord("reference number 2", 2000000000, Successful, "data string", "tax type")))
  }

  "handle an incomplete json object" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(OK, Json.parse("""{"searchScope": "bta"}"""), Map.empty[String, Seq[String]])))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Left("Unable to parse data from payment api")
  }

  "handle an invalid json object" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(OK, Json.toJson("""{"searchScope", }"""), Map.empty[String, Seq[String]])))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Left("Unable to parse data from payment api")
  }

  "handle 400 response" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(BAD_REQUEST, Json.obj(), Map.empty[String, Seq[String]])))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Left("Invalid request sent")
  }

  "handle 404 response" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(NOT_FOUND, Json.obj(), Map.empty[String, Seq[String]])))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Right(Nil)
  }

  "Handle Upstream 4xx response" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.failed(UpstreamErrorResponse("", NOT_FOUND, NOT_FOUND)))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Right(Nil)
  }

  "handle 5xx response" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.failed(UpstreamErrorResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Left("Exception thrown from payment api")
  }

  "handle invalid response code" in {
    when(httpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(HttpResponse.apply(201, Json.obj(), Map.empty[String, Seq[String]])))

    val result = payConnector.get(Vrn(""))

    result.futureValue.leftSide shouldBe Left("Couldn't handle response from payment api")
  }

}