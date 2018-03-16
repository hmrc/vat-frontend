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
import connectors.models._
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import services.VatService
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext.fromLoggingDetails
import uk.gov.hmrc.play.views.formatting.Money.pounds
import views.html.partials.account_summary.vat._

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                     vatService: VatService,
                                     override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getAccountSummaryView(implicit r: AuthenticatedRequest[_]) = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    val breakdownLink = Some(appConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(r.vatDecEnrolment)))

    vatService.fetchVatModel(Some(r.vatDecEnrolment)) map {
      case VatData(accountSummaryData, calendar) => accountSummaryData match {
        case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) =>
          if (amount < 0) {
            account_summary(
              Messages("account.in.credit", pounds(amount.abs, 2)),
              accountSummaryData.openPeriods, calendar, appConfig, breakdownLink, Messages("see.breakdown")
            )
          } else if (amount == 0) {
            account_summary(
              Messages("account.nothing.to.pay"),
              accountSummaryData.openPeriods, calendar, appConfig, breakdownLink, Messages("view.statement")
            )
          } else {
            account_summary(
              Messages("account.due", pounds(amount.abs, 2)),
              accountSummaryData.openPeriods, calendar, appConfig, breakdownLink, Messages("see.breakdown")
            )
          }
        case _ => generic_error(appConfig.getPortalUrl("home")(Some(r.vatDecEnrolment)))
      }
      case VatNoData => account_summary(Messages("account.summary.no.balance.info.to.display"), Seq.empty, None, appConfig)
      case _ => generic_error(appConfig.getPortalUrl("home")(Some(r.vatDecEnrolment)))
    }
  }
}