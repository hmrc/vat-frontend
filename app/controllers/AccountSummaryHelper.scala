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
import connectors.models.{CalendarData, VatModel}
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import services.VatService
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext.fromLoggingDetails
import views.html.partials.account_summary.vat._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                      vatService: VatService,
                                      override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getAccountSummaryView(implicit r: AuthenticatedRequest[_]) = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))
    vatService.fetchVatModel(Some(r.vatDecEnrolment))
  }

  private[controllers] def renderAccountSummaryView(vatModel: VatModel, currentUrl: String, showSubpageLink: Boolean)(implicit r: AuthenticatedRequest[_]) = {
    val summaryData = Future.fromTry( for{
        accountSummaryOpt <- vatModel.accountSummary
      } yield (accountSummaryOpt)
    )

    summaryData.map( data =>
      views.html.partials.account_summary.vat.account_summary(data, vatModel.calendar, currentUrl, showSubpageLink, appConfig)
    )
  }

  private[controllers] def getVatVarsActivationView(currentUrl:String)(implicit r: AuthenticatedRequest[_]) = {
    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))
    Future(views.html.partials.account_summary.vat.vat_var.vat_var_activation(currentUrl,appConfig))
  }
}


