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

package base

import config.FrontendAppConfig
import connectors.models.{AccountSummaryData, CalendarPeriod, DirectDebit, VatData}
import models.{Calendar, InactiveDirectDebit, Monthly}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import utils.EmacUrlBuilder

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite {

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def emacUrlBuilder: EmacUrlBuilder = injector.instanceOf[EmacUrlBuilder]

  def fakeRequest = FakeRequest("", "")

  def messages: Messages = messagesApi.preferred(fakeRequest)

  //Sample data
  val defaultDirectDebit:DirectDebit = DirectDebit(false, None)
  val periodWithOutstandingReturn =  CalendarPeriod(
    DateTime.now.minusMonths(1).toLocalDate,DateTime.now.toLocalDate, None, false
  )
  val periodWithCompletedReturn = CalendarPeriod(
    DateTime.now.minusMonths(1).toLocalDate,DateTime.now.toLocalDate, Some(DateTime.now.minusDays(1).toLocalDate), false
  )

  lazy val calendar: Calendar = Calendar(
    filingFrequency = Monthly,
    directDebit = InactiveDirectDebit
  )
  val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
  val defaultVatData: VatData = VatData(vatAccountSummary, Some(calendar), Some(0))
}
