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

import connectors.models.{CalendarData, VatModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{Lang, Messages}
import play.twirl.api.Html
import sun.security.krb5.internal.AuthContext
import views.html.partials.{account_summary, generic_error}

import scala.util.{Failure, Success}

@Singleton
class Helper @Inject() (messages: Messages) {

  def vatPaymentInterval(vatCalendar: CalendarData)(implicit lang: Lang): String =
    if (vatCalendar.isMonthly) messages("vat.payment.interval.monthly")
    else if (vatCalendar.isQuarterly1) messages("vat.payment.interval.quarterly.1")
    else if (vatCalendar.isQuarterly2) messages("vat.payment.interval.quarterly.2")
    else if (vatCalendar.isQuarterly3) messages("vat.payment.interval.quarterly.3")
    else messages("vat.payment.interval.annually")

  def renderAccountSummary(model: VatModel, currentUrl: String, showSubpageLink: Boolean)(implicit authContext: AuthContext, lang: Lang): Html = {
    model.accountSummary match {
      case Success(accountSummaryOption) =>
//        recordMetrics(model)
        account_summary(accountSummaryOption, model.calendar, currentUrl, showSubpageLink)
      case Failure(e) => generic_error(currentUrl)
    }
  }
}
