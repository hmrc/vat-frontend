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

import controllers.actions._

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatCardBuilderService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import scala.concurrent.ExecutionContext


class PartialController @Inject()(authenticate: AuthAction,
                                  vatCardBuilderService: VatCardBuilderService,
                                  override val controllerComponents: MessagesControllerComponents)(implicit val ec: ExecutionContext)
  extends FrontendController(controllerComponents) with I18nSupport with LoggingUtil {

  def getCard: Action[AnyContent] = authenticate.async { implicit request =>
    vatCardBuilderService.buildVatCard().map(
      card => Ok(toJson(card))
    ).recover {
      case _: Exception =>
        errorLog(s"[PartialController][getCard] - Failed to get data from backend")
        InternalServerError("Failed to get data from backend")
    }
  }

}
