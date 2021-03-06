/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}
import org.joda.time.{LocalDate, Period, PeriodType}
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._

case class PreviouslyFiledVatCalendarPeriod(periodEndDate: LocalDate, returnReceivedDate: LocalDate) {
  private val INITIAL_DATE = new LocalDate(1973, 4, 1)

  def periodCode: Int = {
    val period = new Period(INITIAL_DATE, periodEndDate, PeriodType.months().withDaysRemoved())
    period.getMonths + 1
  }
}

object PreviouslyFiledVatCalendarPeriod {
  implicit val formats: OFormat[PreviouslyFiledVatCalendarPeriod] = Json.format[PreviouslyFiledVatCalendarPeriod]
}
