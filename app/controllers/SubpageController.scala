/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.VatDeferralNewPaymentSchemeConnector
import controllers.actions._
import javax.inject.Inject
import models.VatData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import services.{VatPartialBuilder, VatService}
import services.local.{AccountSummaryHelper, SidebarHelper}
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.subpage

import scala.concurrent.ExecutionContext

class SubpageController @Inject()(appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  subpage: subpage,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  sidebarHelper: SidebarHelper,
                                  vatService: VatService,
                                  vatPartialBuilder: VatPartialBuilder,
                                  paymentHistoryService: PaymentHistoryServiceInterface,
                                  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector,
                                  override val controllerComponents: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext) extends FrontendController(controllerComponents) with I18nSupport {


  def onPageLoad: Action[AnyContent] = (authenticate andThen serviceInfo).async {
    implicit request =>

      val vatModelFuture = vatService.fetchVatModel(request.request.vatDecEnrolment)
      val futurePaymentHistory = paymentHistoryService.getPayments(Some(request.request.vatDecEnrolment))
      val futureVatVar = vatPartialBuilder.buildVatVarPartial(forCard = false)(request.request, messagesApi.preferred(request.request.request), hc)
      val deferralEligibility = vatDeferralNewPaymentSchemeConnector.eligibility(request.request.vatDecEnrolment.vrn.toString)

      for {
        vatModel <- vatModelFuture
        vatVar <- futureVatVar.map(_.getOrElse(HtmlFormat.empty))
        paymentHistory <- futurePaymentHistory
        eligibility <- deferralEligibility
      } yield {
        val summaryView = accountSummaryHelper.getAccountSummaryView(vatModel, paymentHistory, eligibility = eligibility)(request.request)
        val calendarOpt = vatModel match {
          case Right(Some(VatData(_, calendar, _))) => calendar
          case _ => None
        }
        val sidebar = sidebarHelper.buildSideBar(calendarOpt)(request.request)
        Ok(subpage(appConfig, summaryView, sidebar, request.request.vatDecEnrolment, vatVar)(request.serviceInfoContent))
      }
  }

}
