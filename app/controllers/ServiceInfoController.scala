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

package controllers

import connectors.ServiceInfoPartialConnector
import models.requests.{AuthenticatedRequest, ServiceNavigationInfo}
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import services.PartialService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ServiceInfoController @Inject()(serviceInfoPartialConnector: ServiceInfoPartialConnector,
                                      mcc: MessagesControllerComponents,
                                      partialService: PartialService) extends FrontendController(mcc) {

  def serviceInfoPartial[A](request: AuthenticatedRequest[A], activeTab: String = "home")
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ServiceNavigationInfo]] = {
    val maybeNavLinks = serviceInfoPartialConnector.getNavLinks()(hc, ec, request)
    implicit val messages: Messages = mcc.messagesApi.preferred(request.request)
    for {
      navLinks <- maybeNavLinks
    } yield {
      Some(ServiceNavigationInfo(navLinks= partialService.partialList(navLinks), activeTab))
    }
  }

}
