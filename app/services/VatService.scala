/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.ImplementedBy
import connectors.VatConnector
import models._
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex


@ImplementedBy(classOf[VatService])
trait VatServiceInterface {
  def fetchVatModel(vatEnrolment: VatDecEnrolment)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[VatAccountFailure, Option[VatData]]]
  def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[CalendarDerivedInformation]]
}

@Singleton
class VatService @Inject()(vatConnector: VatConnector)(implicit ec: ExecutionContext) extends VatServiceInterface with Logging {

  def fetchVatModel(vatEnrolment: VatDecEnrolment
                   )(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[VatAccountFailure, Option[VatData]]] =
    vatEnrolment match {
      case enrolment@VatDecEnrolment(vrn, true) =>
        vatConnector.accountSummary(vrn).flatMap {
          case Some(accountSummary) => vatCalendar(enrolment).map{ calendarDerivedInformation =>
            Right(Some( VatData(accountSummary,calendarDerivedInformation.map(_.calendar), calendarDerivedInformation.map(_.outstandingReturnCount))))
          }
          case None => Future.successful(Right(None))
        }.recover {
          case _ => Left(VatGenericError)
        }
      case VatDecEnrolment(vrn, false) => Future.successful(Left(VatUnactivated))
    }

  private def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = {
    val regexForAnnual: Regex = "^00(0[4-9]|1[0-5])$".r
    staggerCode match {
      case "0000" => Monthly
      case "0001" => Quarterly(March)
      case "0002" => Quarterly(January)
      case "0003" => Quarterly(February)
      case regexForAnnual(_) => Annually
      case _ => logger.warn(s"The user has an invalid stagger code of $staggerCode")
        InvalidStaggerCode
    }
  }


  def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier, request: Request[_]):  Future[Option[CalendarDerivedInformation]] = {
    vatConnector.calendar(vatEnrolment.vrn).map {

      case Some(calendarData@CalendarData(Some(staggerCode), directDebit, _, _)) =>
        val frequency = determineFrequencyFromStaggerCode(staggerCode)
        val directDebitStatus = DirectDebitStatus.from(calendarData.directDebit)
        Some(CalendarDerivedInformation(Calendar(frequency, directDebitStatus), calendarData.countReturnsToComplete))

      case _ => {
        logger.warn("Received None for calendar")
        None
      }
    } recover {
      case e: Exception =>
        logger.warn(s"Failed to fetch VAT calendar with message - ${e.getMessage}")
        None
    }
  }
}
