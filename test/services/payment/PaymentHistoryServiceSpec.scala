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

package services.payment

import base.SpecBase
import connectors.payments.PaymentHistoryConnectorInterface
import models.{VatDecEnrolment, Vrn}
import models.payment.PaymentStatus.{Invalid, Successful}
import models.payment._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDateTime, OffsetDateTime}
import scala.concurrent.Future

class PaymentHistoryConnectorNotFound extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(Right(Nil))
}

class PaymentHistoryParseError extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(Left("unable to parse data from payment api"))
}

class PaymentHistoryFailed extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.failed(new Throwable)
}

class PaymentHistoryConnectorSingleRecord(
                                           val date: String = "2018-10-20T08:00:21.111",
                                           status: PaymentStatus = Successful ) extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(
    Right(List(createVatPaymentRecord(date, status))))

  def createVatPaymentRecord(date: String, status: PaymentStatus): VatPaymentRecord = {
    VatPaymentRecord(
      reference = "reference number",
      amountInPence = 100,
      status = status,
      createdOn = date,
      taxType = "tax type"
    )
  }
}

class PaymentHistoryConnectorMultiple extends PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier) = Future.successful(
    Right(List(
      VatPaymentRecord(
        reference = "reference number",
        amountInPence = 150: Int,
        status = Successful,
        createdOn = "2018-10-19T08:00:00.000",
        taxType = "tax type"
      ),
      VatPaymentRecord(
        reference = "reference number",
        amountInPence = 100: Int,
        status = Successful,
        createdOn = "2018-10-13T07:59:00.000",
        taxType = "tax type"
      )
    )
    ))
}

class PaymentHistoryServiceSpec extends PlaySpec with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class PaymentHistoryOff extends SpecBase {

    override def fakeApplication() = new GuiceApplicationBuilder()
      .configure(Map("microservice.services.features.get-payment-history" -> false))
      .build()
  }

  class PaymentHistoryOn extends SpecBase

  val date: OffsetDateTime = OffsetDateTime.parse("2018-10-20T08:00:00.000+00:00")

  "PaymentHistoryServiceSpec" when {

    "getPayments is called and getSAPaymentHistory toggle set to true" should {

      "return payment history when valid payment history is returned" in new PaymentHistoryOn {

        val paymentHistorySingleRecord: PaymentHistoryService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord, frontendAppConfig) {
          override val getDateTime: OffsetDateTime = date
        }

        paymentHistorySingleRecord.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Right(List(
          PaymentRecord(
            reference = "reference number",
            amountInPence = 100,
            createdOn = LocalDateTime.parse("2018-10-20T08:00:21.111"),
            taxType = "tax type"
          )
        ))

      }

      "filter payment history that falls outside 7 days" in new PaymentHistoryOn {

        val paymentHistoryConnectorMultiple = new PaymentHistoryService(new PaymentHistoryConnectorMultiple, frontendAppConfig) {
          override val getDateTime = date
        }

        paymentHistoryConnectorMultiple.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Right(List(
          PaymentRecord(
            reference = "reference number",
            amountInPence = 150,
            createdOn = LocalDateTime.parse("2018-10-19T08:00:00.000"),
            taxType = "tax type"
          )
        ))
      }

      "not return payment history when status is not Successful" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord(status = Invalid), frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Right(Nil)
      }

      "not return payment history when payment falls outside of 7 days" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord("2018-10-13T08:01:00.000"), frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Right(Nil)
      }

      "return Right(Nil) when date is invalid format" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorSingleRecord("invalid-date"), frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Right(Nil)
      }

      "return Right(Nil) when payment history could not be found" in new PaymentHistoryOn {
        val paymentService = new PaymentHistoryService(new PaymentHistoryConnectorNotFound, frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Right(Nil)
      }

      "return Left(PaymentRecordFailure) when connector throws exception" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryFailed, frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Left(PaymentRecordFailure)
      }

      "return Left(PaymentRecordFailure) when connector fails to parse" in new PaymentHistoryOn {

        val paymentService = new PaymentHistoryService(new PaymentHistoryParseError, frontendAppConfig)

        paymentService.getPayments(Some(VatDecEnrolment(Vrn("vrn"), true))).futureValue mustBe Left(PaymentRecordFailure)
      }

    }

  }

}