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

package controllers.helpers

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.models._
import models._
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import services.VatService
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.views.formatting.Money.pounds
import views.html.partials.account_summary.vat._
import views.html.partials.account_summary.vat.vat_var.{vat_var_prompt_to_activate, vat_var_prompt_to_enrol}

class AccountSummaryHelper @Inject()(appConfig: FrontendAppConfig,
                                     vatService: VatService,
                                     override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getAccountSummaryView(accountData:VatAccountData)(implicit request: AuthenticatedRequest[_]): Html = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    val breakdownLink = Some(appConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(request.vatDecEnrolment)))

    accountData match {
      case VatData(accountSummaryData, calendar) => accountSummaryData match {
        case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) =>
          val isNotAnnual = calendar match {
            case Some(Calendar(Annually,_)) => false
            case _ => true
          }

          val directDebitContent = buildDirectDebitSection(calendar)
          val vatVarContent = buildVatVarsSection(request.vatDecEnrolment, request.vatVarEnrolment).getOrElse(Html(""))
          if (amount < 0) {
            account_summary(
              Messages("account.in.credit", pounds(amount.abs, 2)),
              accountSummaryData.openPeriods, appConfig, vatVarContent, directDebitContent, breakdownLink, Messages("see.breakdown"),
              showRepaymentContent = isNotAnnual
            )
          } else if (amount == 0) {
            account_summary(
              Messages("account.nothing.to.pay"),
              accountSummaryData.openPeriods, appConfig, vatVarContent, directDebitContent, breakdownLink, Messages("view.statement")
            )
          } else {
            account_summary(
              Messages("account.due", pounds(amount.abs, 2)),
              accountSummaryData.openPeriods, appConfig, vatVarContent, directDebitContent, breakdownLink, Messages("see.breakdown"),
              panelIndent = true
            )
          }
        case _ => generic_error(appConfig.getPortalUrl("home")(Some(request.vatDecEnrolment)))
      }

      case VatNoData =>
        val vatVarContent = buildVatVarsSection(request.vatDecEnrolment, request.vatVarEnrolment).getOrElse(Html(""))
        account_summary(Messages("account.summary.no.balance.info.to.display"), Seq.empty, appConfig, vatVarContent, Html(""))
      case _ => generic_error(appConfig.getPortalUrl("home")(Some(request.vatDecEnrolment)))
    }

  }

  private def buildVatVarsSection(vatDecEnrolment: VatDecEnrolment, vatVarEnrolment: VatEnrolment
                                 )(implicit request: AuthenticatedRequest[_]) : Option[Html] ={
    vatVarEnrolment match {
      case x: VatEnrolment if !x.enrolled  => Some(vat_var_prompt_to_enrol(appConfig,vatDecEnrolment))
      case VatVarEnrolment(_, false) => Some(vat_var_prompt_to_activate(appConfig, vatDecEnrolment, currentUrl = request.request.uri))
      case _ => None
    }
  }

  private def buildDirectDebitSection(calendar: Option[Calendar])(implicit request:AuthenticatedRequest[_]): Html = {
    calendar match {
      case Some(Calendar(filingFrequency, ActiveDirectDebit(details))) if filingFrequency != Annually=> {
        direct_debit_details(details, appConfig)
      }
      case Some(Calendar(filingFrequency,InactiveDirectDebit)) if filingFrequency != Annually => {
        prompt_to_activate_direct_debit(appConfig, request.vatDecEnrolment)
      }
      case _ => Html("")
    }
  }
}



