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

import config.FrontendAppConfig
import controllers.actions._

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ThresholdService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.deregister

class DeregisterController @Inject()(appConfig: FrontendAppConfig,
                                     deregister: deregister,
                                     override val messagesApi: MessagesApi,
                                     authenticate: AuthAction,
                                     serviceInfo: ServiceInfoAction,
                                     thresholdService: ThresholdService,
                                     override val controllerComponents: MessagesControllerComponents
                                    ) extends FrontendController(controllerComponents) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen serviceInfo) {
    implicit request =>
      Ok(deregister(appConfig, thresholdService.formattedVatThreshold())(request.serviceInfoContent))
  }
}
