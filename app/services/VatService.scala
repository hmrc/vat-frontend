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

package services

import com.google.inject.ImplementedBy
import connectors.VatConnector
import connectors.models._
import connectors.models.designatorydetails.DesignatoryDetailsCollection
import javax.inject.{Inject, Singleton}
import models._
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex


@ImplementedBy(classOf[VatService])
trait VatServiceInterface {
  def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData]
  def designatoryDetails(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[DesignatoryDetailsCollection]]
  protected def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency
  def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[Calendar]]
}

@Singleton
class VatService @Inject()(vatConnector: VatConnector)(implicit ec: ExecutionContext) extends VatServiceInterface {

  def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData] = {

    vatEnrolmentOpt match {
      case Some(enrolment@VatDecEnrolment(vrn, true)) =>
        vatConnector.accountSummary(vrn).flatMap {
          case Some(accountSummary) => vatCalendar(enrolment).map(VatData(accountSummary, _))
          case None => Future(VatNoData)
        }.recover {
          case _ => VatGenericError
        }
      case Some(enrolment@VatDecEnrolment(vrn, false)) => Future(VatUnactivated)
      case _ => Future(VatEmpty)
    }
  }

  def designatoryDetails(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[DesignatoryDetailsCollection]] = {
    vatConnector.designatoryDetails(vatEnrolment.vrn).recover {
      case e =>
        Logger.warn(s"Failed to fetch VAT designatory details with message - ${e.getMessage}")
        None
    }
  }

  protected def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = {
    val regexForAnnual: Regex = "^00(0[4-9]|1[0-5])$".r
    staggerCode match {
      case "0000" => Monthly
      case "0001" => Quarterly(March)
      case "0002" => Quarterly(January)
      case "0003" => Quarterly(February)
      case regexForAnnual(_) => Annually
      case _ => Logger.warn(s"The user has an invalid stagger code of $staggerCode")
        InvalidStaggerCode
    }
  }

  def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[Calendar]] = {
    vatConnector.calendar(vatEnrolment.vrn).map {
      case Some(CalendarData(Some(staggerCode), directDebit, _, _)) =>

        val frequency = determineFrequencyFromStaggerCode(staggerCode)

        val directDebitStatus = directDebit match{
          case DirectDebit(true, Some(details)) => ActiveDirectDebit(details)
          case DirectDebit(true, None) => InactiveDirectDebit
          case _ => DirectDebitIneligible
        }
        Some(Calendar(frequency, directDebitStatus))
      case _ => None
    } recover {
      case e =>
        Logger.warn(s"Failed to fetch VAT calendar with message - ${e.getMessage}")
        None
    }
  }

}
