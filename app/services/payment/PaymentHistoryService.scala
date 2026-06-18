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
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggingUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentHistoryService @Inject() (connector: PaymentHistoryConnectorInterface, config: FrontendAppConfig)(implicit val ec: ExecutionContext)
    extends PaymentHistoryServiceInterface
    with LoggingUtil {

  def getPayments(enrolment: Option[VatEnrolment])(implicit
      hc: HeaderCarrier,
      request: Request[_]): Future[Either[PaymentRecordFailure.type, List[PaymentRecord]]] =
    enrolment match {
      case Some(vatEnrolment) =>
        connector
          .get(vatEnrolment.vrn)
          .map {
            case Right(payments) =>
              Right(filterPaymentHistory(payments))
            case Left(message) =>
              warnLog(s"[PaymentHistoryService][getPayments] - PaymentRecordFailure: $message")
              Left(PaymentRecordFailure)
          }
          .recover { case e =>
            warnLog(s"[PaymentHistoryService][getPayments] - PaymentRecordFailure: ${e.getMessage}")
            Left(PaymentRecordFailure)
          }
      case None =>
        warnLog(s"[PaymentHistoryService][getPayments] - Failed: No VAT enrolment")
        Future.successful(Right(Nil))
    }

  private def filterPaymentHistory(payments: List[VatPaymentRecord]): List[PaymentRecord] =
    payments.flatMap(PaymentRecord.from)

}

@ImplementedBy(classOf[PaymentHistoryService])
trait PaymentHistoryServiceInterface {
  def getPayments(enrolment: Option[VatEnrolment])(implicit
      hc: HeaderCarrier,
      request: Request[_]): Future[Either[PaymentRecordFailure.type, List[PaymentRecord]]]
}
