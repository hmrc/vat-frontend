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

package controllers

import javax.inject.Inject

import akka.actor.Status.Success
import config.FrontendAppConfig
import connectors.models.VatModel
import controllers.actions._
import controllers.helpers.SidebarHelper
import models.Helper
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{subpage2, subpage_aggregated}

import scala.util.Try

class SubpageController @Inject()(appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  helper: Helper,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  sidebarHelper: SidebarHelper) extends FrontendController with I18nSupport {

  def onPageLoad = (authenticate andThen serviceInfo).async {
    implicit request =>
      accountSummaryHelper.getAccountSummaryView(request.request).map {
        accountSummaryView => {

          implicit val authRequest: AuthenticatedRequest[_] = request.request
          implicit val baseRequest: Request[_] = request.request.request

          val vatModel = VatModel(Try(None), None)

          Ok(subpage2(vatModel,routes.SubpageController.onPageLoad().absoluteURL()(request),appConfig, helper, accountSummaryView)(request.serviceInfoContent)
            (baseRequest,messagesApi.preferred(baseRequest),authRequest))

  }

  def onPageLoadAggregateSubpage = (authenticate andThen serviceInfo).async {
    implicit request =>
      accountSummaryHelper.getAccountSummaryView(request.request).flatMap {
        vatModel => {
          val currenturl = routes.SubpageController.onPageLoad().absoluteURL()
          val vatVarSidebarSummary = for {
            vatVar <- accountSummaryHelper.getVatVarsActivationView(currenturl)(request.request)
            sidebar <- sidebarHelper.buildSideBar(vatModel.calendar)(request.request)
            accountSummary <- accountSummaryHelper.renderAccountSummaryView(vatModel, currenturl, false)(request.request)
          } yield (vatVar, sidebar, accountSummary)
          vatVarSidebarSummary.map(
            tuple => Ok(subpage_aggregated(appConfig, tuple._3, tuple._2, tuple._1, request.request.vatDecEnrolment)(request.serviceInfoContent))
          )
        }
      }
  }
}
