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

package controllers.actions

import models.requests.{AuthenticatedRequest, ListLinks, ServiceInfoRequest, ServiceNavigationInfo}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}


object FakeServiceInfoAction extends ServiceInfoAction {
  override protected def transform[A](request: AuthenticatedRequest[A]): Future[ServiceInfoRequest[A]] = {
    val listLinks = Seq(ListLinks(message = "Home", url = "/home"))
    val serviceNavigation: ServiceNavigationInfo = ServiceNavigationInfo(navLinks = listLinks)

    Future.successful(ServiceInfoRequest(request, serviceNavigation))
  }

  override protected def executionContext: ExecutionContext = global
}
