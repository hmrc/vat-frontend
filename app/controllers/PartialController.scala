/*
 * Copyright 2019 HM Revenue & Customs
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
import controllers.helpers.AccountSummaryHelper
import javax.inject.Inject
import models.{Card, Link, VatNoEnrolment}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent}
import services.{VatCardBuilderService, VatServiceInterface}
import models.requests.AuthenticatedRequest
import services.{EnrolmentsStoreService, VatServiceInterface, VatVarPartialBuilder}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.EmacUrlBuilder
import views.html.partial

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PartialController @Inject()(
                                  val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  appConfig: FrontendAppConfig,
                                  vatService: VatServiceInterface,
                                  vatCardBuilderService: VatCardBuilderService,
                                  vatVarPartialBuilder: VatVarPartialBuilder
                                  ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate.async {
    implicit request =>
      vatService.fetchVatModel(Some(request.vatDecEnrolment)).map(
        vatModel => {
          val accountView = accountSummaryHelper.getAccountSummaryView(vatModel, showCreditCardMessage = false)
          Ok(partial(request.vatDecEnrolment.vrn, appConfig, accountView))
        }
      )
  }

  def getCard: Action[AnyContent] = authenticate.async { implicit request =>
    vatCardBuilderService.buildVatCard().map( card => {
      Ok(toJson(card))
    }).recover {
      case _: Exception => InternalServerError("Failed to get data from backend")
    }
  }

}
