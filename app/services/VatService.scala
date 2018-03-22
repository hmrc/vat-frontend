/*
 * Copyright 2018 HM Revenue & Customs
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

package services

import javax.inject.{Inject, Singleton}

import connectors.VatConnector
import connectors.models.{AccountSummaryData, CalendarData, VatModel}
import models._
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc.Request

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class VatService @Inject()(vatConnector: VatConnector) {

  def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[VatModel] = {
    for (
      accountSummary <- accountSummary(vatEnrolmentOpt);
      vatCalendar <- vatCalendar(vatEnrolmentOpt)
    ) yield {
      VatModel(accountSummary, vatCalendar)
    }
  }

  def designatoryDetails(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier) = {
    vatConnector.designatoryDetails(vatEnrolment.vrn).recover {
      case e  =>
        Logger.warn(s"Failed to fetch VAT designatory details with message - ${e.getMessage}")
        None
    }
  }

  def accountSummary(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[Try[Option[AccountSummaryData]]] = {
    vatEnrolmentOpt match {
      case Some(e @ VatDecEnrolment(_, true)) =>
        vatConnector.accountSummary(e.vrn).map {
          case Some(x) if(x.isValid) => Success(Some(x))
          case None => Success(None)
        }
      case _ => Future(Failure(new IllegalArgumentException("VAT account not found")))
    }
  }

  def vatCalendar(vatEnrolmentOpt: Option[VatEnrolment])(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[CalendarData]] = {
    vatEnrolmentOpt match {
      case Some(enrolment @ VatDecEnrolment(_, true)) =>
        vatConnector.calendar(enrolment.vrn).recover {
          case e : IllegalArgumentException =>
            None
          case e =>
            Logger.warn(s"Failed to fetch VAT calendar with message - ${e.getMessage}")
            None
        }
      case _ => Future.successful(None)
    }
  }

}
