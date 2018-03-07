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

package models

import javax.inject.{Inject, Singleton}

import config.FrontendAppConfig
import connectors.models._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{Lang, Messages}
import play.twirl.api.Html
import views.html.partials.account_summary.vat._
import metrics.Metrics
import play.api.mvc._

import scala.util.{Failure, Success}



@Singleton
class Helper @Inject()(appConfig: FrontendAppConfig) {
  def recordMetrics(vatModel: VatModel): Unit = {
    vatModel.consolidatedPeriodData.status match {
      case SuccessfulMatch => Metrics.markVatSummaryAndCalendarMatch()
      case PartialOrNoMatch => Metrics.markVatSummaryAndCalendarMismatch()
      case EmptyAccountSummaryResponse => Metrics.markVatSummaryAndCalendarSummaryEmpty()
      case EmptyCalendarResponse => Metrics.markVatSummaryAndCalendarCalendarEmpty()
      case BothResponsesEmpty => Metrics.markVatSummaryAndCalendarBothEmpty()
      case FailedResponse => Metrics.markVatSummaryAndCalendarError()
      case _ => // don't do anything
    }
  }

  def renderAccountSummaryView(model: VatModel, currentUrl: String)(implicit request: Request[_]): Html = {
    wrapper(renderAccountSummary(model, currentUrl, showSubpageLink = true))
  }

  def renderAccountSummary(model: VatModel, currentUrl: String, showSubpageLink: Boolean)(implicit request: Request[_]): Html = {
    model.accountSummary match {
      case Success(accountSummaryOption) =>
        recordMetrics(model)
        account_summary(accountSummaryOption, model.calendar, currentUrl, showSubpageLink, appConfig)

        // @(accountSummaryOpt: Option[AccountSummaryData], vatCalendarOpt: Option[CalendarData], currentUrl: String, showSubpageLink: Boolean, appConfig: FrontendAppConfig)(implicit request: Request[_], messages: Messages)

      case Failure(e) => generic_error(currentUrl)
    }
  }

  def vatPaymentInterval(vatCalendar: CalendarData)(implicit lang: Lang): String =
    if (vatCalendar.isMonthly) Messages("vat.payment.interval.monthly")
    else if (vatCalendar.isQuarterly1) Messages("vat.payment.interval.quarterly.1")
    else if (vatCalendar.isQuarterly2) Messages("vat.payment.interval.quarterly.2")
    else if (vatCalendar.isQuarterly3) Messages("vat.payment.interval.quarterly.3")
    else Messages("vat.payment.interval.annually")

}

// TODO:
object Helper
