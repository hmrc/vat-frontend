/*
 * Copyright 2024 HM Revenue & Customs
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

package services.local

import config.FrontendAppConfig
import models._
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import javax.inject.Inject

class SidebarHelper @Inject()(appConfig: FrontendAppConfig,
                              override val messagesApi: MessagesApi
                             ) extends I18nSupport with HeaderCarrierConverter {

  def buildSideBar(optCalendar: Option[Calendar])(implicit request: AuthenticatedRequest[_]): Html = {
    val sidebarScheduleHtml = buildFilingCalendarSection(optCalendar)
    views.html.partials.sidebar_links(request.vatDecEnrolment, appConfig, sidebarScheduleHtml)
  }

  private def buildFilingCalendarSection(optCalendar: Option[Calendar])(implicit request: AuthenticatedRequest[_]): Html = {
    val html = optCalendar map { calendar: Calendar =>
      calendar.filingFrequency match {
        case Monthly => views.html.partials.sidebar.filing_calendar_monthly(appConfig, request.vatDecEnrolment)
        case Annually => views.html.partials.sidebar.filing_calendar_annually(appConfig)
        case Quarterly(January) => views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly_2", appConfig, request.vatDecEnrolment)
        case Quarterly(February) => views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly_3", appConfig, request.vatDecEnrolment)
        case Quarterly(March) => views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly_1", appConfig, request.vatDecEnrolment)
        case InvalidStaggerCode => views.html.partials.sidebar.filing_calendar_missing(appConfig, request.vatDecEnrolment)
      }
    }

    html.getOrElse(views.html.partials.sidebar.filing_calendar_missing(appConfig, request.vatDecEnrolment))
  }

}
