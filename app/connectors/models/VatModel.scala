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

package connectors.models

import play.api.libs.json.{Json, OFormat}

import scala.util.{Success, Try}


case class VatModel(accountSummary: Try[Option[AccountSummaryData]], calendar: Option[CalendarData]) {

  lazy val consolidatedPeriodData: ConsolidatedPeriodResult = {
    (accountSummary, calendar) match {
      case (Success(Some(summary)), Some(cal)) if summary.openPeriods.nonEmpty =>
        val calendarPeriods = cal.currentPeriod.toList ++ cal.previousPeriods
        val matchingCalendarPeriods = summary.openPeriods flatMap { p => calendarPeriods.find(_.periodEndDate == p.openPeriod) }
        if (matchingCalendarPeriods.size != summary.openPeriods.size) {
          ConsolidatedPeriodResult(PartialOrNoMatch)
        } else {
          val consolidatedPeriods = matchingCalendarPeriods map { cp => ConsolidatedPeriod(cp.periodStartDate, cp.periodEndDate, cp.periodAnnAccInd) }
          ConsolidatedPeriodResult(SuccessfulMatch, consolidatedPeriods)
        }
      case (Success(Some(summary)), Some(cal)) => ConsolidatedPeriodResult(NoOpenPeriods)
      case (Success(None), Some(_)) => ConsolidatedPeriodResult(EmptyAccountSummaryResponse)
      case (Success(Some(_)), None) => ConsolidatedPeriodResult(EmptyCalendarResponse)
      case (Success(None), None) => ConsolidatedPeriodResult(BothResponsesEmpty)
      case _ => ConsolidatedPeriodResult(FailedResponse)
    }
  }
}
