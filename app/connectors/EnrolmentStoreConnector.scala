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

package connectors

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import _root_.models.UserEnrolments
import config.FrontendAppConfig
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


@Singleton
class EnrolmentStoreConnectorImpl @Inject()(override val http:  HttpClient, config: FrontendAppConfig)
                                           (implicit val ec:ExecutionContext) extends EnrolmentStoreConnector{

  val host = config.enrolmentStoreUrl
  def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {

    http.GET[HttpResponse](buildURL(credId)).map{

      x => x.status match {

        case Status.OK => {
          Try(x.json.as[UserEnrolments]) match {
            case Success(r) => Right(r)
            case Failure(a) => Left("unable to parse data from enrolment API")
          }
        }
        case _ => Left(errorMessage(x))

      }

    }.recover({
      case _ : Exception => Left("exception thrown from enrolment API")
    })
  }

  def errorMessage(response: HttpResponse): String = {
    response.status match{
      case Status.NOT_FOUND => "user not found from enrolment API"
      case Status.BAD_REQUEST => "bad request to enrolment API"
      case Status.FORBIDDEN => "forbidden from enrolment API"
      case Status.SERVICE_UNAVAILABLE => "unexpected error from enrolment API"
      case Status.NO_CONTENT => "no content from enrolment API"
      case _:Int => "enrolment API couldn't handle response code"
    }
  }

  private def buildURL(credId: String): String = s"$host/enrolment-store/users/$credId/enrolments?service=IR-SA"
}

@ImplementedBy(classOf[EnrolmentStoreConnectorImpl])
trait EnrolmentStoreConnector{
  def http: HttpClient
  def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]]
}
