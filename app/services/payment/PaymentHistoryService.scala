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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import connectors.payments.PaymentHistoryConnectorInterface
import models.VatEnrolment
import models.payment.{PaymentRecord, PaymentRecordFailure, VatPaymentRecord}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDateTime
import scala.concurrent.Future

@Singleton
class PaymentHistoryService @Inject()(connector: PaymentHistoryConnectorInterface, config: FrontendAppConfig) extends PaymentHistoryServiceInterface {


  def getPayments(enrolment: Option[VatEnrolment])(implicit hc: HeaderCarrier): Future[Either[PaymentRecordFailure.type, List[PaymentRecord]]] =
      enrolment match {
        case Some(vatEnrolment) => {
          connector.get(vatEnrolment.vrn).map {
            case Right(payments) => {
              Right(filterPaymentHistory(payments, getDateTime))
            }
            case Left(message) => {
              log(message)

            }
          }.recover {
            case _ => Left(PaymentRecordFailure)
          }
      }
        case None => Future.successful(Right(Nil))
      }

  private def log(x: String): Either[PaymentRecordFailure.type, List[PaymentRecord]] = {
    val logger: Logger = Logger(this.getClass)
    logger.warn(s"[PaymentHistoryService][getPayments] $x")

    Left(PaymentRecordFailure)
  }

  private def filterPaymentHistory(payments: List[VatPaymentRecord],  currentDate: LocalDateTime): List[PaymentRecord] = {
    payments.flatMap(PaymentRecord.from(_, currentDate))
  }


  protected[services] def getDateTime: LocalDateTime = {
    LocalDateTime.now()
  }

}

@ImplementedBy(classOf[PaymentHistoryService])
trait PaymentHistoryServiceInterface {
  def getPayments(enrolment: Option[VatEnrolment])(implicit hc: HeaderCarrier): Future[Either[PaymentRecordFailure.type, List[PaymentRecord]]]
  protected[services] def getDateTime: LocalDateTime
}
