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
import connectors.models.CalendarData
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.HeaderCarrierConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SidebarHelper @Inject()(appConfig: FrontendAppConfig,
                              override val messagesApi: MessagesApi
                             ) extends I18nSupport {

  private[controllers] def buildSideBar(optCalendar: Option[CalendarData])(implicit r: AuthenticatedRequest[_]) = {
    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))
    val sidebarScheduleHtml = buildFilingCalendarSection(optCalendar)
    Future(views.html.partials.sidebar_links(r.vatDecEnrolment, appConfig, sidebarScheduleHtml))
  }

  private def buildFilingCalendarSection(optCalendar: Option[CalendarData])(implicit r: AuthenticatedRequest[_]) = {
    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))
    optCalendar match {
      case Some(calendar) if calendar.isMonthly =>  views.html.partials.sidebar.filing_calendar_monthly(appConfig)
      case Some(calendar) if calendar.isAnnual =>  views.html.partials.sidebar.filing_calendar_annual(appConfig)
      case Some(calendar) if calendar.isQuarterly1 => {
        views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly.1",appConfig, r.vatDecEnrolment)
      }
      case Some(calendar) if calendar.isQuarterly2 => {
        views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly.2",appConfig, r.vatDecEnrolment)
      }
      case Some(calendar) if calendar.isQuarterly3 => {
        views.html.partials.sidebar.filing_calendar_quarterly("subpage.sidebar.quarterly.3",appConfig, r.vatDecEnrolment)
      }
      case _ => views.html.partials.sidebar.filing_calendar_missing(appConfig,r.vatDecEnrolment)

    }
  }
}
