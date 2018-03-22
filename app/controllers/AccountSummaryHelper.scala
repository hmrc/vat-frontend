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

import config.FrontendAppConfig
import connectors.models._
import javax.inject.Inject

import models.{Annually, Calendar}
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import services.VatService
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.views.formatting.Money.pounds
import views.html.partials.account_summary.vat._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                     vatService: VatService,
                                     override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getAccountSummaryView(accountData:VatAccountData)(implicit r: AuthenticatedRequest[_]): Html = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    val breakdownLink = Some(appConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(r.vatDecEnrolment)))

    accountData match {
      case VatData(accountSummaryData, calendar) => accountSummaryData match {
        case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) =>
          val ddEligible = calendar.fold(false)(_.directDebit.ddiEligibilityInd)
          val ddActive = calendar.fold[Option[DirectDebitActive]](None)(_.directDebit.active)
          val isNotAnnual = calendar match {
            case Some(Calendar(Annually,_)) => false
            case _ => true
          }
          if (amount < 0) {
            account_summary(
              Messages("account.in.credit", pounds(amount.abs, 2)),
              accountSummaryData.openPeriods, appConfig, breakdownLink, Messages("see.breakdown"),
              ddEligible,
              ddActive,
              showRepaymentContent = isNotAnnual
            )
          } else if (amount == 0) {
            account_summary(
              Messages("account.nothing.to.pay"),
              accountSummaryData.openPeriods, appConfig, breakdownLink, Messages("view.statement"),
              ddEligible,
              ddActive
            )
          } else {
            account_summary(
              Messages("account.due", pounds(amount.abs, 2)),
              accountSummaryData.openPeriods, appConfig, breakdownLink, Messages("see.breakdown"),
              ddEligible,
              ddActive
            )
          }
        case _ => generic_error(appConfig.getPortalUrl("home")(Some(r.vatDecEnrolment)))
      }
      case VatNoData => account_summary(Messages("account.summary.no.balance.info.to.display"), Seq.empty, appConfig, ddEligible = false, ddActive = None)
      case _ => generic_error(appConfig.getPortalUrl("home")(Some(r.vatDecEnrolment)))
    }

  }

  private[controllers] def getVatVarsActivationView(currentUrl:String)(implicit r: AuthenticatedRequest[_]) = {
    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))
    Future(views.html.partials.account_summary.vat.vat_var.vat_var_activation(currentUrl,appConfig))
  }

}



