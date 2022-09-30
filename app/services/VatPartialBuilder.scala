/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import models._
import models.requests.AuthenticatedRequest
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.{EmacUrlBuilder, LoggingUtil}
import views.html.partials.account_summary.vat.vat_var.{prompt_to_enrol_card, vat_var_prompt_to_enrol}
import views.html.partials.vat.card.payments.{payments_fragment_just_credit, payments_fragment_no_data, payments_fragment_no_tax, payments_fragment_upcoming_bill_active_dd}
import views.html.partials.vat.card.returns.{multiple_returns, no_returns, one_return, returns_fragment_no_data}

import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatPartialBuilderImpl @Inject()(val enrolmentsStore: EnrolmentsStoreService,
                                      emacUrlBuilder: EmacUrlBuilder,
                                      payments_fragment_upcoming_bill_active_dd: payments_fragment_upcoming_bill_active_dd,
                                      payments_fragment_just_credit: payments_fragment_just_credit,
                                      appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) extends VatPartialBuilder with LoggingUtil {

  override def buildReturnsPartial(vatData: VatData, vatEnrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = {
    vatData.returnsToCompleteCount match {
      case Some(0) =>
        warnLog("Showing no returns card")
        no_returns(appConfig, Some(vatEnrolment))
      case Some(1) =>
        one_return(appConfig, Some(vatEnrolment))
      case Some(returnCount) if returnCount > 1 =>
        multiple_returns(appConfig, Some(vatEnrolment), returnCount)
      case _ =>
        warnLog("Returns data not available")
        returns_fragment_no_data(appConfig, Some(vatEnrolment))
    }
  }

  override def buildPaymentsPartial(vatData: VatData)(
    implicit request: AuthenticatedRequest[_], messages: Messages): Html =
    vatData.accountSummary.accountBalance.flatMap(_.amount) match {
      case Some(amount) if amount > 0 =>
        buildPaymentsPartialInDebit(vatData.calendar, amount)
      case Some(amount) if amount == 0 =>
        payments_fragment_no_tax(appConfig)
      case Some(amount) if amount < 0 =>
        payments_fragment_just_credit(amount.abs)
      case _ =>
        payments_fragment_no_data()
    }

  private def buildPaymentsPartialInDebit(calendar: Option[Calendar], amount: BigDecimal)(
    implicit request: AuthenticatedRequest[_], messages: Messages): Html = {
    calendar match {
      case Some(Calendar(filingFrequency, ActiveDirectDebit(ddActiveDetails))) if filingFrequency != Annually =>
        payments_fragment_upcoming_bill_active_dd(amount.abs, ddActiveDetails, appConfig, request.vatDecEnrolment)
      case Some(Calendar(filingFrequency, InactiveDirectDebit)) if filingFrequency != Annually =>
        views.html.partials.vat.card.payments.payments_fragment_upcoming_bill_inactive_dd(amount.abs, appConfig, request.vatDecEnrolment)
      case _ =>
        views.html.partials.vat.card.payments.payments_fragment_upcoming_bill(amount.abs, appConfig, request.vatDecEnrolment)
    }
  }

  private def buildVatVarEnrolmentPrompt(forCard: Boolean)(implicit request: AuthenticatedRequest[_], messages: Messages): Option[Html] =
    if (forCard) {
      Some(prompt_to_enrol_card(emacUrlBuilder, request.vatDecEnrolment))
    } else {
      Some(vat_var_prompt_to_enrol(emacUrlBuilder, request.vatDecEnrolment))
    }

  private def buildVatVarNotActivatedPrompt(
                                             forCard: Boolean, showPin: Boolean
                                           )(
                                             implicit request: AuthenticatedRequest[_], messages: Messages
                                           ): Option[Html] = {

    val varCurrentUrl: String = if (forCard) {
      appConfig.businessAccountHomeUrl
    } else {
      request.uri
    }

    if (showPin) {
      Some(
        views.html.partials.account_summary.vat.vat_var.prompt_to_activate_new_pin(
          emacUrlBuilder, request.vatDecEnrolment, appConfig, varCurrentUrl, forCard
        )
      )
    } else {
      Some(
        views.html.partials.account_summary.vat.vat_var.prompt_to_activate_no_new_pin(
          emacUrlBuilder, request.vatDecEnrolment, appConfig, varCurrentUrl, forCard
        )
      )
    }
  }

  def buildVatVarPartial(
                          forCard: Boolean
                        )(
                          implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier
                        ): Future[Option[Html]] =
    request.vatVarEnrolment match {
      case _: VatNoEnrolment =>
        Future.successful(buildVatVarEnrolmentPrompt(forCard))
      case VatVarEnrolment(_, false) =>
        enrolmentsStore.showNewPinLink(request.vatVarEnrolment, OffsetDateTime.now(ZoneOffset.UTC), request.credId).map(
          showPin => buildVatVarNotActivatedPrompt(forCard, showPin)
        )
      case _ => Future.successful(None)
    }

}

@ImplementedBy(classOf[VatPartialBuilderImpl])
trait VatPartialBuilder {
  def buildReturnsPartial(vatData: VatData, vatEnrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html

  def buildPaymentsPartial(vatData: VatData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html

  def buildVatVarPartial(forCard: Boolean)(implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]]
}
