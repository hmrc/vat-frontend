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

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.models.VatData
import controllers.actions._
import controllers.helpers.{AccountSummaryHelper, SidebarHelper}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.twirl.api.Html
import services.{VatService, VatVarPartialBuilder}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.subpage

import scala.concurrent.ExecutionContext

class SubpageController @Inject()(appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  sidebarHelper: SidebarHelper,
                                  vatService: VatService,
                                  vatVarPartialBuilder: VatVarPartialBuilder)(implicit ec:ExecutionContext) extends FrontendController with I18nSupport {


  def onPageLoad = (authenticate andThen serviceInfo).async {
    implicit request =>
      val futureModelVatVar = for{
        model <-vatService.fetchVatModel(Some(request.request.vatDecEnrolment))
        vatVar <- vatVarPartialBuilder.getPartialForSubpage(request.request.vatVarEnrolment, request.request.vatDecEnrolment)
      } yield{
        (model,vatVar)
      }

        futureModelVatVar.map(
        modelVatVar => {
          val vatModel = modelVatVar._1
          val vatVar = modelVatVar._2.getOrElse(Html(""))
          val summaryView = accountSummaryHelper.getAccountSummaryView(vatModel)(request.request)
          val calendarOpt = vatModel match {
            case VatData(_, calendar) => calendar
            case _ => None
          }
          val sidebar = sidebarHelper.buildSideBar(calendarOpt)(request.request)
          Ok(subpage(appConfig, summaryView, sidebar, request.request.vatDecEnrolment, vatVar)(request.serviceInfoContent))
        }
      )

  }
}
