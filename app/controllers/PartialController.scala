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
import controllers.actions._
import models._
import models.Helper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.partial
import scala.concurrent.ExecutionContext.Implicits.global

class PartialController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  helper: Helper,
                                  appConfig: FrontendAppConfig
                                 ) extends FrontendController with I18nSupport {

  def onPageLoad = authenticate.async  {
    implicit request =>
      val currentUrl = routes.SubpageController.onPageLoad().absoluteURL()
      val summaryVatVars = for{
        vatModel <- accountSummaryHelper.getAccountSummaryView
        accountSummary <-accountSummaryHelper.renderAccountSummaryView(vatModel, currentUrl, false)
        vatVar <- accountSummaryHelper.getVatVarsActivationView(currentUrl)
      } yield (accountSummary, vatVar)

      summaryVatVars.map{ tuple =>
        Ok(partial(request.vatDecEnrolment.vrn, tuple._1, tuple._2, appConfig))
      }
  }
}