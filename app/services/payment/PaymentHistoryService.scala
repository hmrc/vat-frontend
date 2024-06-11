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

package services.payment

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import connectors.payments.PaymentHistoryConnectorInterface
import models.VatEnrolment
import models.payment.{PaymentRecord, PaymentRecordFailure, VatPaymentRecord}
import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggingUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentHistoryService @Inject()(connector: PaymentHistoryConnectorInterface, config: FrontendAppConfig)(implicit val ec: ExecutionContext) extends
  PaymentHistoryServiceInterface with LoggingUtil{


  def getPayments(enrolment: Option[VatEnrolment])(implicit hc: HeaderCarrier, request: Request[_]): Future[Either[PaymentRecordFailure.type,
    List[PaymentRecord]]] =
      enrolment match {
        case Some(vatEnrolment) =>
          connector.get(vatEnrolment.vrn).map {
            case Right(payments) =>
              infoLog(s"[PaymentHistoryService][getPayments] - Succeeded with $payments ")
              Right(filterPaymentHistory(payments))
            case Left(message) => log(message)
          }.recover {
            case _ =>
              warnLog(s"[PaymentHistoryService][getPayments] - Failed with: paymentRecordFailure")
              Left(PaymentRecordFailure)
          }
        case None =>
          warnLog(s"[PaymentHistoryService][getPayments] - Failed with: no VAT enrolment")
          Future.successful(Right(Nil))
      }

  private def log(x: String): Either[PaymentRecordFailure.type, List[PaymentRecord]] = {
    val logger: Logger = Logger(this.getClass)

    Left(PaymentRecordFailure)
  }

  private def filterPaymentHistory(payments: List[VatPaymentRecord]): List[PaymentRecord] = {
    payments.flatMap(PaymentRecord.from(_))
  }


}

@ImplementedBy(classOf[PaymentHistoryService])
trait PaymentHistoryServiceInterface {
  def getPayments(enrolment: Option[VatEnrolment])(implicit hc: HeaderCarrier, request: Request[_]): Future[Either[PaymentRecordFailure.type,
    List[PaymentRecord]]]
}
