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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.http._
import config.FrontendAppConfig
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._
import utils.LoggingUtil
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.mvc.Request

@Singleton
class PayConnector @Inject()(http: HttpClient, config: FrontendAppConfig) extends LoggingUtil{
  private def payApiBaseUrl: String = config.payApiUrl

  private def paymentsFrontendBaseUrl: String = config.getUrl("paymentsFrontendBase")

  def vatPayLink(startPaymentJourneyRequest: StartPaymentJourneyBtaVat)
                (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Future[NextUrl] = {
    infoLog(s"[PayConnector][vatPayLink] - Attempted to retrieve vat pay link")
    http.POST[StartPaymentJourneyBtaVat, NextUrl](s"$payApiBaseUrl/pay-api/bta/vat/journey/start", startPaymentJourneyRequest)
  }.recover({
    case e: Exception =>
      warnLog(s"[PayConnector][vatPayLink] - Failed with: ${e.getMessage}")
      NextUrl(s"$paymentsFrontendBaseUrl/service-unavailable")
  })

}

final case class StartPaymentJourneyBtaVat(
                                            amountInPence: Long,
                                            returnUrl: String,
                                            backUrl: String,
                                            vrn: String
                                          )

object StartPaymentJourneyBtaVat {
  implicit val format: Format[StartPaymentJourneyBtaVat] = Json.format[StartPaymentJourneyBtaVat]
}

final case class NextUrl(nextUrl: String)

object NextUrl {
  implicit val nextUrlFormat: Format[NextUrl] = Json.format[NextUrl]
}

final case class VatPeriod(month: Int, year: Int)

object VatPeriod {
  implicit val vatPeriodFormat: OFormat[VatPeriod] = Json.format[VatPeriod]
}
