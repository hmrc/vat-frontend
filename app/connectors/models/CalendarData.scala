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

import org.joda.time.LocalDate
import play.api.libs.json.{Json, OFormat}


case class CalendarData(staggerCode: Option[String], directDebit: DirectDebit, currentPeriod: Option[CalendarPeriod], previousPeriods: Seq[CalendarPeriod]){

  import CalendarData._

  require(staggerCode.exists(regexForValid.matcher(_).matches()))
  val isMonthly = staggerCode.contains("0000")
  val isQuarterly1 = staggerCode.contains("0001")
  val isQuarterly2 = staggerCode.contains("0002")
  val isQuarterly3 = staggerCode.contains("0003")
  val isAnnual = staggerCode.exists(regexForAnnual.matcher(_).matches())
  val isQuarterly = isQuarterly1 || isQuarterly2 || isQuarterly3
  val isNotAnnual = isQuarterly || isMonthly

  def hasPreviouslyFiledReturn = {
    previouslyFiledVatCalendarPeriods.nonEmpty
  }

  def previouslyFiledVatCalendarPeriods = {
    val allPeriods = currentPeriod.toList ++ previousPeriods
    allPeriods.collect { case CalendarPeriod(_, endDate, Some(returnDate), _) => PreviouslyFiledVatCalendarPeriod(endDate, returnDate) }
  }

  def mostRecentReturnReceivedPeriod = {
    val orderedFiledPeriods = previouslyFiledVatCalendarPeriods sortWith { (period1, period2) => period1.returnReceivedDate.isAfter(period2.returnReceivedDate) }
    orderedFiledPeriods.headOption
  }

  def isEligibleForDirectDebit = {
    directDebit.ddiEligibilityInd && !isAnnual
  }
}

object CalendarData {
  implicit val formats: OFormat[CalendarData] = Json.format[CalendarData]
  val regexForAnnual = "^00(0[4-9]|1[0-5])$".r.pattern
  val regexForValid = "^00(0[0-9]|1[0-5])$".r.pattern
}
