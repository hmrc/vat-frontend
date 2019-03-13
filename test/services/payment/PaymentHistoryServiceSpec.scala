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

package services.payment

import base.SpecBase
import connectors.payments.PaymentHistoryConnectorInterface
import models.VatDecEnrolment
import models.payment._
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class PaymentHistoryConnectorNotFound extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(Right(PaymentHistoryNotFound))
}

class PaymentHistoryParseError extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(Left("unable to parse data from payment api"))
}

class PaymentHistoryFailed extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.failed(new Throwable)
}

class PaymentHistoryConnectorSingleRecord(val date: String = "2018-10-20T08:00:00.000", status: PaymentStatus = Successful) extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(
    Right(PaymentHistory(
      searchScope = "bta",
      searchTag = "search-tag",
      payments = List(
        PaymentRecord(
          reference = "reference number",
          amountInPence = 100,
          status = status,
          createdOn = date,
          taxType = "tax type"
        )
      )
    )))
}

class PaymentHistoryConnectorMultiple extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(
    Right(PaymentHistory(
      searchScope = "bta",
      searchTag = "search-tag",
      payments = List(
        PaymentRecord(
          reference = "reference number",
          amountInPence = 150: Int,
          status = Successful,
          createdOn = "2018-10-19T08:00:00.000",
          taxType = "tax type"
        ),
        PaymentRecord(
          reference = "reference number",
          amountInPence = 100: Int,
          status = Successful,
          createdOn = "2018-10-13T08:01:00.000",
          taxType = "tax type"
        )
      )
    )))
}

class PaymentHistoryServiceSpec extends PlaySpec with ScalaFutures  {

  implicit val hc = HeaderCarrier()

  class PaymentHistoryOff extends SpecBase {

    override def fakeApplication() = new GuiceApplicationBuilder()
      .configure(Map("microservice.services.features.get-payment-history" -> false))
      .build()
  }

  class PaymentHistoryOn extends SpecBase

  val date = new DateTime("2018-10-20T08:00:00.000").toLocalDate

  "PaymentHistoryServiceSpec" when {

    "getPayments is called and getSAPaymentHistory toggle set to false" should {

      "return Nil" in new PaymentHistoryOff {

        val paymentHistorySingleRecord = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord, frontendAppConfig) {
          override val getDateTime = date
        }

        paymentHistorySingleRecord.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

    }

    "getPayments is called and getSAPaymentHistory toggle set to true" should {

      "return payment history when valid payment history is returned" in new PaymentHistoryOn {

        val paymentHistorySingleRecord = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord, frontendAppConfig) {
          override val getDateTime = date
        }

        paymentHistorySingleRecord.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe List(
          PaymentRecord(
            reference = "reference number",
            amountInPence = 100,
            status = Successful,
            createdOn = "2018-10-20T08:00:00.000",
            taxType = "tax type"
          )
        )
      }

      "return payment history when payments fall within and outside 7 days" in new PaymentHistoryOn {

        val paymentHistoryConnectorMultiple = new PaymentHistoryService(new PaymentHistoryConnectorMultiple, frontendAppConfig) {
          override val getDateTime = date
        }

        paymentHistoryConnectorMultiple.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe List(
          PaymentRecord(
            reference = "reference number",
            amountInPence = 150,
            status = Successful,
            createdOn = "2018-10-19T08:00:00.000",
            taxType = "tax type"
          )
        )
      }

      "not return payment history when status is not Successful" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord(status = Created), frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

      "not return payment history when payment falls outside of 7 days" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord("2018-10-13T08:01:00.000"), frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

      "return Nil when date is invalid format" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord("invalid-date"), frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

      "return Nil when payment history could not be found" in new PaymentHistoryOn {
        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorNotFound, frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

      "return Nil when connector throws exception" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryFailed, frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

      "return Nil when connector fails to parse" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryParseError, frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Nil
      }

    }

  }

}