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

import config.FrontendAppConfig
import connectors.models.VatData
import controllers.actions._
import controllers.helpers.{AccountSummaryHelper, SidebarHelper}
import play.api.i18n.{I18nSupport, MessagesApi}
import services.VatService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.subpage

class SubpageController @Inject()(appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  sidebarHelper: SidebarHelper,
                                  vatService: VatService) extends FrontendController with I18nSupport {


  def onPageLoad = (authenticate andThen serviceInfo).async {
    implicit request =>
      vatService.fetchVatModel(Some(request.request.vatDecEnrolment)).map(
        vatModel => {
          val summaryView = accountSummaryHelper.getAccountSummaryView(vatModel)(request.request)
          val calendarOpt = vatModel match {
            case VatData(_, calendar) => calendar
            case _ => None
          }
          val sidebar = sidebarHelper.buildSideBar(calendarOpt)(request.request)
          Ok(subpage(appConfig, summaryView, sidebar, request.request.vatDecEnrolment)(request.serviceInfoContent))
        }
      )

  }
}
