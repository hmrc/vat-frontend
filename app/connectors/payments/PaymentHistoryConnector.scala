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

package connectors.payments

import akka.actor.ActorSystem
import com.google.inject.{ImplementedBy, Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.http.ws.WSGet
import com.typesafe.config.Config
import config.FrontendAppConfig
import models.payment.{PaymentHistory, PaymentHistoryInterface, PaymentHistoryNotFound}
import play.api.Configuration
import play.api.http.Status
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class PaymentHistoryConnector @Inject()(http: WSHttpImplementation, config: FrontendAppConfig) extends PaymentHistoryConnectorInterface {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val payApiUrl = config.getUrl("payApiBase")

  val searchScope: String = "BTA"

  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier): Future[Either[String, PaymentHistoryInterface]] = {
    http.GET[HttpResponse](buildUrl(searchTag.vrn)).map {
      r => r.status match {
        case Status.OK => {
          Try(r.json.as[PaymentHistory]) match {
            case Success(data) => Right(data)
            case Failure(_) => Left("Unable to parse data from payment api")
          }
        }
        case Status.NOT_FOUND => Right(PaymentHistoryNotFound)
        case Status.BAD_REQUEST => Left("Invalid request sent")
        case _ => Left("Couldn't handle response from payment api")
      }
    }
      .recover({
        case _ : NotFoundException => Right(PaymentHistoryNotFound)
        case _ : Exception => Left("Exception thrown from payment api")
      })
  }

  private def buildUrl(searchTag: String, taxType: String = "vat") = s"$payApiUrl/payment/search/$searchScope/$searchTag?taxType=$taxType"
}

@ImplementedBy(classOf[PaymentHistoryConnector])
trait PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier): Future[Either[String, PaymentHistoryInterface]]
}

@ImplementedBy(classOf[WSHttpImplementation])
trait WSHttp extends WSGet with HttpGet


class WSHttpImplementation @Inject() (config:Configuration, override val actorSystem: ActorSystem) extends WSHttp  {
  override val hooks = NoneRequired
  override def configuration: Option[Config] = Option(config.underlying)
}