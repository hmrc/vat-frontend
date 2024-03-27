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

import config.FrontendAppConfig

import javax.inject.{Inject, Singleton}
import models.requests.{AuthenticatedRequest, NavContent}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances}
import utils.LoggingUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceInfoPartialConnector @Inject()(val http: HttpClient, val config: FrontendAppConfig)
  extends LoggingUtil with HttpReadsInstances {

  lazy val btaUrl: String = config.btaUrl + "/business-account/partial/service-info"
  lazy val btaNavLinksUrl: String = config.btaUrl + "/business-account/partial/nav-links"

  def getNavLinks[A]()(implicit hc: HeaderCarrier, ec: ExecutionContext, request: AuthenticatedRequest[A]): Future[Option[NavContent]] = {
    http.GET[Option[NavContent]](s"$btaNavLinksUrl")
      .recover{
        case e =>
          warnLog(s"[ServiceInfoPartialConnector][getNavLinks] - Unexpected error ${e.getMessage}")
          None
      }
  }

}
