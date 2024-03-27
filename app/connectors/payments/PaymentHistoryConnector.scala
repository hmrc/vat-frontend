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

package connectors.payments

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.Vrn
import models.payment.VatPaymentRecord
import play.api.http.Status
import play.api.libs.json.JsSuccess
import play.api.mvc.Request
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpResponse, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import utils.LoggingUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentHistoryConnector @Inject()(http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends PaymentHistoryConnectorInterface with LoggingUtil {

  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, List[VatPaymentRecord]]] =
    http.GET[HttpResponse](buildUrl(searchTag.vrn)).map { response =>
      response.status match {
        case Status.OK =>
          (response.json \ "payments").validate[List[VatPaymentRecord]] match {
            case JsSuccess(paymentHistory, _) =>
              infoLog("[PaymentHistoryConnector][get] - Successfully retrieved payment history")
              Right(paymentHistory)
            case _ =>
              warnLog("[PaymentHistoryConnector][get] - Failed to retrieve payment history: Unable to parse data from payment api")
              Left("Unable to parse data from payment api")
          }
        case Status.NOT_FOUND =>
          warnLog("[PaymentHistoryConnector][get] - Unable to retrieve payment history: Payment history not found")
          Right(Nil)
        case Status.BAD_REQUEST =>
          errorLog("[PaymentHistoryConnector][get] - Unable to retrieve payment history: Invalid request")
          Left("Invalid request sent")
        case _ =>
          errorLog("[PaymentHistoryConnector][get] - Unable to retrieve payment history: Couldn't handle response from payment api")
          Left("Couldn't handle response from payment api")
      }
    }.recover({
      case e: NotFoundException =>
        errorLog(s"[PaymentHistoryConnector][get] - Failed with ${e.getMessage}")
        Right(Nil)
      case UpstreamErrorResponse.Upstream4xxResponse(error) if error.statusCode == 404 =>
        errorLog(s"[PaymentHistoryConnector][get] - Failed with ${error.getMessage}")
        Right(Nil)
      case e: BadRequestException =>
        errorLog(s"[PaymentHistoryConnector][get] - Failed with ${e.getMessage}")
        Left("Invalid request sent")
      case e: Exception =>
        errorLog(s"[PaymentHistoryConnector][get] - Failed with ${e.getMessage}")
        Left("Exception thrown from payment api")
    })

  private def buildUrl(searchTag: String) = s"${config.payApiUrl}/pay-api/v2/payment/search/$searchTag?taxType=vat&searchScope=BTA"
}

@ImplementedBy(classOf[PaymentHistoryConnector])
trait PaymentHistoryConnectorInterface {
  def get(searchTag: Vrn)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, List[VatPaymentRecord]]]
}
