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

package connectors

import _root_.models.UserEnrolments
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import play.api.http.Status
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class EnrolmentStoreConnectorImpl @Inject()(val http: HttpClient, config: FrontendAppConfig)
                                           (implicit val ec:ExecutionContext) extends EnrolmentStoreConnector with LoggingUtil {

  val host: String = config.enrolmentStoreProxyUrl

  def getEnrolments(credId : String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
    http.GET[HttpResponse](buildURL(credId)).map{ response =>
      response.status match {
        case Status.OK =>
          Try(response.json.as[UserEnrolments]) match {
            case Success(r) =>
              infoLog(s"[EnrolmentStoreConnector][getEnrolments] - Successfully retrieved user enrolments")
              Right(r)
            case Failure(_) =>
              warnLog(s"[EnrolmentStoreConnector][getEnrolments] - Unable to parse data from enrolment API")
              Left("Unable to parse data from enrolment API")
          }
        case _ =>
          warnLog(s"[EnrolmentStoreConnector][getEnrolments] - ${errorMessage(response)}")
          Left(errorMessage(response))
      }

    }.recover({
      case e : Exception =>
        warnLog(s"[EnrolmentStoreConnector][getEnrolments] ${e.getMessage}")
        Left(e.getMessage)
    })
  }

  def errorMessage(response: HttpResponse): String = {
    response.status match{
      case Status.NOT_FOUND => "User not found from enrolment API"
      case Status.BAD_REQUEST => "Bad request to enrolment API"
      case Status.FORBIDDEN => "Forbidden from enrolment API"
      case Status.SERVICE_UNAVAILABLE => "Unexpected error from enrolment API"
      case Status.NO_CONTENT => "No content from enrolment API"
      case _:Int => "Enrolment API couldn't handle response code"
    }
  }

  private def buildURL(credId: String): String = {
    s"$host/enrolment-store/users/$credId/enrolments?service=HMCE-VATVAR-ORG"
  }
}

@ImplementedBy(classOf[EnrolmentStoreConnectorImpl])
trait EnrolmentStoreConnector{
  def http: HttpClient
  def getEnrolments(credId :String) (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]]
}
