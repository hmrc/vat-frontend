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
import org.joda.time.LocalDate
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.{Html, HtmlFormat}
import services.{VatPartialBuilder, VatService}
import services.VatService
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.subpage

import scala.concurrent.{ExecutionContext, Future}

class SubpageController @Inject()(appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  sidebarHelper: SidebarHelper,
                                  vatService: VatService,
                                  vatPartialBuilder: VatPartialBuilder,
                                  paymentHistoryService: PaymentHistoryServiceInterface)(implicit ec: ExecutionContext) extends FrontendController with I18nSupport {


  def onPageLoad: Action[AnyContent] = (authenticate andThen serviceInfo).async {
    implicit request =>

      val vatModelFuture = vatService.fetchVatModel(request.request.vatDecEnrolment)
      val futurePaymentHistory = paymentHistoryService.getPayments(Some(request.request.vatDecEnrolment))
      val futureVatVar = vatPartialBuilder.buildVatVarPartial(forCard = false)(request.request, messagesApi.preferred(request.request.request), hc)

      for {
        vatModel <- vatModelFuture
        vatVar <- futureVatVar.map(_.getOrElse(HtmlFormat.empty))
        paymentHistory <- futurePaymentHistory
      } yield {
        val summaryView = accountSummaryHelper.getAccountSummaryView(vatModel, paymentHistory)(request.request)
        val calendarOpt = vatModel match {
          case Right(Some(VatData(_, calendar))) => calendar
          case _ => None
        }
        val sidebar = sidebarHelper.buildSideBar(calendarOpt)(request.request)
        Ok(subpage(appConfig, summaryView, sidebar, request.request.vatDecEnrolment, vatVar)(request.serviceInfoContent))
      }
  }

}
