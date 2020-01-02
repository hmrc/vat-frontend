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

package connectors

import config.FrontendAppConfig
import connectors.models._
import javax.inject.{Inject, Singleton}
import play.api.http.Status._
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatConnector @Inject()(val http: HttpClient, val config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  lazy val vatUrl: String = config.vatUrl

  private def handleResponse[A](uri: String)(implicit rds: HttpReads[A]): HttpReads[Option[A]] = new HttpReads[Option[A]] {
    override def read(method: String, url: String, response: HttpResponse): Option[A] = response.status match {
      case OK => Some(rds.read(method, url, response))
      case NO_CONTENT | NOT_FOUND => None
      case _ => throw MicroServiceException(
        s"Unexpected response status: ${response.status} (possible further details: ${response.body}) for call to $uri",
        response
      )
    }
  }

  def accountSummary(vrn: Vrn)(implicit hc: HeaderCarrier): Future[Option[AccountSummaryData]] = {
    val uri: String = vatUrl + s"/vat/$vrn/accountSummary"
    http.GET[Option[AccountSummaryData]](uri)(handleResponse[AccountSummaryData](uri), hc, ec)
  }

  def calendar(vrn: Vrn)(implicit hc: HeaderCarrier): Future[Option[CalendarData]] = {
    val uri: String = vatUrl + s"/vat/$vrn/calendar"
    http.GET[Option[CalendarData]](uri)(handleResponse[CalendarData](uri), hc, ec)
  }

}
