/*
 * Copyright 2019 HM Revenue & Customs
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

import base.SpecBase
import connectors.models.{CalendarData, CalendarPeriod, DirectDebit}
import org.joda.time.DateTime

class CalendarDataSpec extends SpecBase {

  val defaultDirectDebit:DirectDebit = DirectDebit(false, None)
  val periodWithOutstandingReturn =  CalendarPeriod(DateTime.now.minusMonths(1).toLocalDate,DateTime.now.toLocalDate, None, false)
  val periodWithCompletedReturn =  CalendarPeriod(
    DateTime.now.minusMonths(1).toLocalDate,DateTime.now.toLocalDate, Some(DateTime.now.minusDays(1).toLocalDate), false
  )

  "The hasReturns to complete method" when{
    "currentPeriod is none and previousPeriods is empty" should{
      "return false" in {
        val testModel = CalendarData(None, defaultDirectDebit, None, Nil)
        testModel.hasReturnsToComplete() mustBe false
      }
    }

    "currentPeriod has a return without a received date and previousPeriods is empty" should {
      "return true" in {
        val testModel = CalendarData(None, defaultDirectDebit, Some(periodWithOutstandingReturn), Nil)
        testModel.hasReturnsToComplete() mustBe true
      }
    }

    "currentPeriod has a return with a received date and previousPeriods is empty" should {
      "return false" in {
        val testModel = CalendarData(None, defaultDirectDebit, Some(periodWithCompletedReturn), Nil)
        testModel.hasReturnsToComplete() mustBe false
      }
    }

    "currentPeriod is none and previousPeriods has only returns with received dates" should {
      "return false" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, None, Seq(periodWithCompletedReturn, periodWithCompletedReturn))
        testModel.hasReturnsToComplete() mustBe false
      }
    }

    "currentPeriod is none and previousPeriods has a return without a received date" should {
      "return true" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, None, Seq(periodWithCompletedReturn, periodWithOutstandingReturn))
        testModel.hasReturnsToComplete() mustBe true
      }
    }

    "currentPeriod has a return with a received date and previousPeriods has only returns with received dates" should {
      "return false" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithCompletedReturn), Seq(periodWithCompletedReturn, periodWithCompletedReturn))
        testModel.hasReturnsToComplete() mustBe false
      }
    }

    "currentPeriod has a return with a received date and previousPeriods has a return without a received date" should {
      "return true" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithCompletedReturn), Seq(periodWithCompletedReturn, periodWithOutstandingReturn))
        testModel.hasReturnsToComplete() mustBe true
      }
    }

    "currentPeriod has a return without a received date and previousPeriods has only returns with received dates" should {
      "return true" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithOutstandingReturn), Seq(periodWithCompletedReturn, periodWithCompletedReturn))
        testModel.hasReturnsToComplete() mustBe true
      }
    }

    "currentPeriod has a return without a received date and previousPeriods has a return without a received date" should {
      "return true" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithOutstandingReturn), Seq(periodWithCompletedReturn, periodWithOutstandingReturn))
        testModel.hasReturnsToComplete() mustBe true
      }
    }
  }


}
