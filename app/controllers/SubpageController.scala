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

  def onPageLoad2 = (authenticate andThen serviceInfo).async {
    implicit request =>
      accountSummaryHelper.getAccountSummaryView(request.request).flatMap {
        vatModel => {

          //          implicit val authRequest: AuthenticatedRequest[_] = request.request
          //          implicit val baseRequest: Request[_] = request.request.request
          //
          //          //Ok(subpage(vatModel, routes.SubpageController.onPageLoad().absoluteURL(), appConfig)(request.serviceInfoContent))
          //          Ok(subpage2(vatModel,routes.SubpageController.onPageLoad().absoluteURL()(request),appConfig, helper)(request.serviceInfoContent)
          //            (baseRequest,messagesApi.preferred(baseRequest),authRequest))
          val accountSummary = Html("<p> Account summary goes here </p>")
          sidebarHelper.buildSideBar(vatModel.calendar)(request.request).map(
            sidebar => Ok(subpage_aggregated(appConfig, accountSummary, sidebar, request.request.vatDecEnrolment)(request.serviceInfoContent))
          )
        }
      }
  }


  /*
  Copied from VatController (BTA)
  def renderSubpage()(implicit userProfile: UserProfile, authContext: AuthContext, request: Request[AnyRef]) = {
    val futureVatModel = vatConnectivity.vatModel(userProfile.vatEnrolment)

    futureVatModel map { vatModel =>
      checkVatEnrolmentAndPresentPage{vat_subpage(vatModel, routes.VatController.subpage().absoluteURL())}
    }
  }

   */
}
