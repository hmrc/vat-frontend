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

import javax.inject.Inject

import connectors.EnrolmentStoreConnector
import models.{UserEnrolmentStatus, UserEnrolments, VatVarEnrolment}
import org.joda.time.{DateTimeZone, LocalDate}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


class EnrolmentStoreServiceImpl @Inject()(connector: EnrolmentStoreConnector)(implicit val ec:ExecutionContext) extends EnrolmentsStoreService {

  val daysBetweenExpectedArrivalAndExpiry = 23

  override def showActivationLink( enrolment: Option[VatVarEnrolment], currentDate: LocalDate)(implicit hc: HeaderCarrier): Future[Boolean] = enrolment match {
    case Some(VatVarEnrolment(vrn, false)) => {
      val enrolmentDetailsList: Future[Either[String, UserEnrolments]] = connector.getEnrolments(vrn.toString())
      enrolmentDetailsList.map({
        case Right(UserEnrolments(y)) if y.nonEmpty => {

          val a: Seq[UserEnrolmentStatus] = y.filter(_.enrolmentTokenExpiryDate.isDefined).sortWith { (left, right) =>
            left.enrolmentTokenExpiryDate.get.isAfter(right.enrolmentTokenExpiryDate.get)
          }

          a match {
            case Nil => true
            case _ =>
              val expectedArrivalDate =
                a.head.enrolmentTokenExpiryDate.get.minusDays(daysBetweenExpectedArrivalAndExpiry).toDateTime(DateTimeZone.UTC).getMillis
              currentDate.toDateTimeAtCurrentTime.isAfter(expectedArrivalDate)          }

        }
        case Right(_) => true
        case _ => true
      })
    }
    case Some(VatVarEnrolment(_,true)) => Future(false)
    case _ => Future(true)
  }
}

trait EnrolmentsStoreService {
  def showActivationLink( enrolment: Option[VatVarEnrolment], currentDate: LocalDate)(implicit hc: HeaderCarrier): Future[Boolean]
}
