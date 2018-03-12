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
import connectors.models.CalendarData
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import services.VatService
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext.fromLoggingDetails
import views.html.account_summary_from_scratch
import views.html.partials.account_summary.vat._

import scala.concurrent.Future

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                      vatService: VatService,
                                      override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getVatModel(implicit r: AuthenticatedRequest[_]) = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    // TODO:This needs to call accountSummary
    //and vatCalendar and display them in the page as a VatModel

    //TODO: remove the .get
    vatService.fetchVatModel(Some(r.vatDecEnrolment))
//
//    vatService.fetchVatModel(Some(r.vatDecEnrolment)).map(
//      vatModel =>
//    )

    //TODO - either have this not be an option, or stop constraining to a SOME when we generate and handle Option properly here
//    vatService.fetchVatModel(Some(r.vatDecEnrolment.get)).flatMap(
//      vatModel => {
//        Future.fromTry(vatModel.accountSummary.map(
//            accountSummaryOpt => Future(views.html.partials.account_summary.vat.account_summary(accountSummaryOpt, vatModel.calendar, currentUrl = "", showSubpageLink = true,              appConfig))
//          )
//        )
//      }
//    )
    //@(accountSummaryOpt: Option[AccountSummaryData], vatCalendarOpt: Option[CalendarData], currentUrl: String, showSubpageLink: Boolean, appConfig: FrontendAppConfig)(implicit request: Request[_], messages: Messages)

    //Future(generic_error(appConfig.getPortalUrl("home")(r.vatEnrolment)))

  }
  private[controllers] def getAccountSummaryView(currentUrl: String)(implicit r: AuthenticatedRequest[_]) = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    // TODO:This needs to call accountSummary
    //and vatCalendar and display them in the page as a VatModel

    //TODO: remove the .get
    //vatService.fetchVatModel(Some(r.vatDecEnrolment))
    //
        vatService.fetchVatModel(Some(r.vatDecEnrolment)).flatMap(
          vatModel => {
            Future.fromTry(vatModel.accountSummary).map(
              accountSummaryData => account_summary_from_scratch(appConfig, r.vatVarEnrolment, currentUrl, r.vatDecEnrolment, accountSummaryData)
            )
          }

        )

    //TODO - either have this not be an option, or stop constraining to a SOME when we generate and handle Option properly here
    //    vatService.fetchVatModel(Some(r.vatDecEnrolment.get)).flatMap(
    //      vatModel => {
    //        Future.fromTry(vatModel.accountSummary.map(
    //            accountSummaryOpt => Future(views.html.partials.account_summary.vat.account_summary(accountSummaryOpt, vatModel.calendar, currentUrl = "", showSubpageLink = true,              appConfig))
    //          )
    //        )
    //      }
    //    )
    //@(accountSummaryOpt: Option[AccountSummaryData], vatCalendarOpt: Option[CalendarData], currentUrl: String, showSubpageLink: Boolean, appConfig: FrontendAppConfig)(implicit request: Request[_], messages: Messages)

    //Future(generic_error(appConfig.getPortalUrl("home")(r.vatEnrolment)))
  }
}


