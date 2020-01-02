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

package models

import base.SpecBase
import connectors.models.CalendarData

class CalendarDataSpec extends SpecBase {

  "The returnsToCompleteCount method" when{
    "currentPeriod is none and previousPeriods is empty" should{
      "return 0" in {
        val testModel = CalendarData(None, defaultDirectDebit, None, Nil)
        testModel.countReturnsToComplete mustBe 0
      }
    }

    "currentPeriod has a return without a received date and previousPeriods is empty" should {
      "return 1" in {
        val testModel = CalendarData(None, defaultDirectDebit, Some(periodWithOutstandingReturn), Nil)
        testModel.countReturnsToComplete mustBe 1
      }
    }

    "currentPeriod has a return with a received date and previousPeriods is empty" should {
      "return 0" in {
        val testModel = CalendarData(None, defaultDirectDebit, Some(periodWithCompletedReturn), Nil)
        testModel.countReturnsToComplete mustBe 0
      }
    }

    "currentPeriod is none and previousPeriods has only returns with received dates" should {
      "return 0" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, None, Seq(periodWithCompletedReturn, periodWithCompletedReturn))
        testModel.countReturnsToComplete mustBe 0
      }
    }

    "currentPeriod is none and previousPeriods has one return without a received date" should {
      "return 1" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, None, Seq(periodWithCompletedReturn, periodWithOutstandingReturn))
        testModel.countReturnsToComplete mustBe 1
      }
    }

    "currentPeriod is none and previousPeriods has two returns without a received date" should {
      "return 2" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, None, Seq(periodWithOutstandingReturn, periodWithOutstandingReturn))
        testModel.countReturnsToComplete mustBe 2
      }
    }

    "currentPeriod has a return with a received date and previousPeriods has only returns with received dates" should {
      "return 0" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithCompletedReturn), Seq(periodWithCompletedReturn, periodWithCompletedReturn))
        testModel.countReturnsToComplete mustBe 0
      }
    }

    "currentPeriod has a return with a received date and previousPeriods has a return without a received date" should {
      "return 1" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithCompletedReturn), Seq(periodWithCompletedReturn, periodWithOutstandingReturn))
        testModel.countReturnsToComplete mustBe 1
      }
    }

    "currentPeriod has a return without a received date and previousPeriods has only returns with received dates" should {
      "return 1" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithOutstandingReturn), Seq(periodWithCompletedReturn, periodWithCompletedReturn))
        testModel.countReturnsToComplete mustBe 1
      }
    }

    "currentPeriod has a return without a received date and previousPeriods has a return without a received date" should {
      "return 2" in {
        val testModel = CalendarData(
          None, defaultDirectDebit, Some(periodWithOutstandingReturn), Seq(periodWithCompletedReturn, periodWithOutstandingReturn))
        testModel.countReturnsToComplete mustBe 2
      }
    }
  }

}
