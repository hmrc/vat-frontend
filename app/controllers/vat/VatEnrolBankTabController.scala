/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.vat

import javax.inject.Inject

import config.FrontendAppConfig
import controllers.actions.{AuthAction, ServiceInfoAction}
import models.VatDecEnrolment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.EmacUrlBuilder
import views.html.partials.vat.vat_enrol_bank_tab

import scala.concurrent.Future

class VatEnrolBankTabController @Inject()(
 override val messagesApi: MessagesApi,
 authenticate: AuthAction,
 serviceInfo: ServiceInfoAction,
 appConfig: FrontendAppConfig
) extends FrontendController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = authenticate.async {
    implicit request =>
      Future.successful(Ok(vat_enrol_bank_tab(new EmacUrlBuilder(appConfig), request.vatDecEnrolment)))
  }
}