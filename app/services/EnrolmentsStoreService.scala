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

package services

import com.google.inject.ImplementedBy
import connectors.EnrolmentStoreConnector
import javax.inject.{Inject, Singleton}
import models.{UserEnrolmentStatus, UserEnrolments, VatEnrolment, VatVarEnrolment}
import org.joda.time.{DateTime, DateTimeZone}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentStoreServiceImpl @Inject()(connector: EnrolmentStoreConnector)(implicit val ec:ExecutionContext) extends EnrolmentsStoreService {

  val daysBetweenExpectedArrivalAndExpiry = 23

  override def showNewPinLink(enrolment: VatEnrolment, currentDate: DateTime, credId : String)(
    implicit hc: HeaderCarrier): Future[Boolean] = enrolment match {
    case VatVarEnrolment(_, false) =>
      val enrolmentDetailsList: Future[Either[String, UserEnrolments]] = connector.getEnrolments(credId)
      enrolmentDetailsList.map {
        case Right(UserEnrolments(y)) if y.nonEmpty =>

          val a: Seq[UserEnrolmentStatus] = y.filter(_.enrolmentTokenExpiryDate.isDefined).sortWith { (left, right) =>
            left.enrolmentTokenExpiryDate.get.isAfter(right.enrolmentTokenExpiryDate.get)
          }

          a match {
            case Nil => true
            case _ =>
              val expectedArrivalDate =
                a.head.enrolmentTokenExpiryDate.get.minusDays(daysBetweenExpectedArrivalAndExpiry).toDateTime(DateTimeZone.UTC).getMillis
              currentDate.isAfter(expectedArrivalDate)
          }
        case _ => true
      }
    case VatVarEnrolment(_,true) => Future.successful(false)
    case _ => Future.successful(true)
  }
}

@ImplementedBy(classOf[EnrolmentStoreServiceImpl])
trait EnrolmentsStoreService {
  def showNewPinLink(enrolment: VatEnrolment, currentDate: DateTime, credId : String)(implicit hc: HeaderCarrier): Future[Boolean]
}
