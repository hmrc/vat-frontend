/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.Vrn
import models.payment.VatPaymentRecord
import play.api.http.Status
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpResponse, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentHistoryConnector @Inject()(http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends PaymentHistoryConnectorInterface {

  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier): Future[Either[String, List[VatPaymentRecord]]] =
    http.GET[HttpResponse](buildUrl(searchTag.vrn)).map { response =>
      response.status match {
        case Status.OK =>
          (response.json \ "payments").validate[List[VatPaymentRecord]] match {
            case JsSuccess(paymentHistory, _) => Right(paymentHistory)
            case _ => Left("Unable to parse data from payment api")
          }
        case Status.NOT_FOUND => Right(Nil)
        case Status.BAD_REQUEST => Left("Invalid request sent")
        case _ => Left("Couldn't handle response from payment api")
      }
    }.recover({
      case _: NotFoundException => Right(Nil)
      case UpstreamErrorResponse.Upstream4xxResponse(error) if error.statusCode == 404 => Right(Nil)
      case _: BadRequestException => Left("Invalid request sent")
      case _: Exception => Left("Exception thrown from payment api")
    })

  private def buildUrl(searchTag: String) = s"${config.payApiUrl}/pay-api/payment/search/BTA/$searchTag?taxType=vat"
}

@ImplementedBy(classOf[PaymentHistoryConnector])
trait PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier): Future[Either[String, List[VatPaymentRecord]]]
}
