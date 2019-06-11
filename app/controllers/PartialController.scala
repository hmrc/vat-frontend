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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.HtmlFormat
import services.payment.PaymentHistoryServiceInterface
import services.{VatCardBuilderService, VatPartialBuilder, VatServiceInterface}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.partial

import scala.concurrent.ExecutionContext.Implicits.global


class PartialController @Inject()(
                                   val messagesApi: MessagesApi,
                                   authenticate: AuthAction,
                                   accountSummaryHelper: AccountSummaryHelper,
                                   appConfig: FrontendAppConfig,
                                   vatService: VatServiceInterface,
                                   vatCardBuilderService: VatCardBuilderService,
                                   vatPartialBuilder: VatPartialBuilder,
                                   paymentHistoryService: PaymentHistoryServiceInterface
                                 ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate.async { implicit request =>
    val vatModelFuture = vatService.fetchVatModel(request.vatDecEnrolment)
    val futurePaymentHistory = paymentHistoryService.getPayments(Some(request.vatDecEnrolment))
    val futureVatVar = vatPartialBuilder.buildVatVarPartial(forCard = false)

    for {
      maybeVatModel <- vatModelFuture
      vatVar <- futureVatVar.map(_.getOrElse(HtmlFormat.empty))
      maybePaymentHistory <- futurePaymentHistory
    } yield {
      val accountView = accountSummaryHelper.getAccountSummaryView(maybeVatModel, maybePaymentHistory, showCreditCardMessage = false)
      Ok(partial(request.vatDecEnrolment.vrn, appConfig, accountView, vatVar))
    }
  }

  def getCard: Action[AnyContent] = authenticate.async { implicit request =>
    vatCardBuilderService.buildVatCard().map(
      card => Ok(toJson(card))
    ).recover {
      case _: Exception => InternalServerError("Failed to get data from backend")
    }
  }

}
