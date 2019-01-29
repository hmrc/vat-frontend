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
import models.VatNotAddedFormModel
import play.api.data
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.unauthorised
import play.api.data.Form
import play.api.data.Forms._
import forms.VatNotAddedForm

import scala.concurrent.Future


class UnauthorisedController @Inject()(val appConfig: FrontendAppConfig,
                                       val messagesApi: MessagesApi,
                                       val vatNotAddedForm: VatNotAddedForm) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Unauthorized(views.html.unauthorised(vatNotAddedForm.form, appConfig))
  }

  def processForm: Action[AnyContent] = Action { implicit request =>
    vatNotAddedForm.form.bindFromRequest.fold(
      (formWithErrors: Form[VatNotAddedFormModel]) => {
        BadRequest(views.html.unauthorised(formWithErrors, appConfig))
      },
      (success: VatNotAddedFormModel) => {
        success.radioOption match {
          case Some("sign_in_to_other_account")     => Redirect(appConfig.businessAccountWrongCredsUrl)
          case Some("add_your_vat_to_this_account") => Redirect(appConfig.addVatUrl)
          case _                                    => Unauthorized(views.html.unauthorised(vatNotAddedForm.form, appConfig))
        }
      }
    )
  }

}
