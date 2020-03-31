/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import javax.inject.Inject
import models._
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.play.HeaderCarrierConverter

class SidebarHelper @Inject()(appConfig: FrontendAppConfig,
                              override val messagesApi: MessagesApi
                             ) extends I18nSupport {

  private[controllers] def buildSideBar(optCalendar: Option[Calendar])(implicit r: AuthenticatedRequest[_]) = {
    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    val sidebarScheduleHtml = buildFilingCalendarSection(optCalendar)
    views.html.partials.sidebar_links(r.vatDecEnrolment, appConfig, sidebarScheduleHtml)
  }

  private def buildFilingCalendarSection(optCalendar: Option[Calendar])(implicit r: AuthenticatedRequest[_]): Html = {
    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    val html = optCalendar map { calendar: Calendar =>
      calendar.filingFrequency match {
        case Monthly => views.html.partials.sidebar.filing_calendar_monthly(appConfig, r.vatDecEnrolment)
        case Annually => views.html.partials.sidebar.filing_calendar_annually(appConfig)
        case Quarterly(January) => views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly_2", appConfig, r.vatDecEnrolment)
        case Quarterly(February) => views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly_3", appConfig, r.vatDecEnrolment)
        case Quarterly(March) => views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly_1", appConfig, r.vatDecEnrolment)
        case InvalidStaggerCode => views.html.partials.sidebar.filing_calendar_missing(appConfig, r.vatDecEnrolment)
      }
    }

    html.getOrElse(views.html.partials.sidebar.filing_calendar_missing(appConfig, r.vatDecEnrolment))
  }
}
