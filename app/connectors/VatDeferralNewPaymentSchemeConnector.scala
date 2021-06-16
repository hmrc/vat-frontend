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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.Eligibility
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import scala.concurrent.{ExecutionContext, Future}

class VatDeferralNewPaymentSchemeConnector @Inject()(http: HttpClient,
                                                     config: FrontendAppConfig) extends Logging {

  lazy val serviceURL: String = config.servicesConfig.baseUrl("vat-deferral-new-payment-scheme-service")

  def eligibility(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    val url = s"$serviceURL/vat-deferral-new-payment-scheme/eligibility/$vrn"
    http.GET[Eligibility](url).map{
      case Eligibility(Some(true),_,_,_,_)           => Some("Payment Exists")
      case Eligibility(_,_,_,Some(false),Some(true)) => Some("Eligible")
      case _ => None
    }
  }.recover{
    case e: Exception =>
      logger.error(s"[VatDeferralNewPaymentSchemeConnector][eligibility] Failed with error: ${e}")
      Some("API Error")

  }
}
