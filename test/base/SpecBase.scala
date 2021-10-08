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

package base

import config.FrontendAppConfig
import models._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.{FakeRequest, Injecting}
import utils.EmacUrlBuilder
import java.time.LocalDateTime

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  def frontendAppConfig: FrontendAppConfig = inject[FrontendAppConfig]

  def messagesApi: MessagesApi = inject[MessagesApi]

  def emacUrlBuilder: EmacUrlBuilder = inject[EmacUrlBuilder]

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  implicit def lang(implicit request: Request[_]): Lang = inject[MessagesApi].preferred(request).lang

  def messages: Messages = messagesApi.preferred(fakeRequest)

  def defaultDirectDebit: DirectDebit = SpecBase.defaultDirectDebit

  def periodWithOutstandingReturn: CalendarPeriod = SpecBase.periodWithOutstandingReturn

  def periodWithCompletedReturn: CalendarPeriod = SpecBase.periodWithCompletedReturn

  def calendar: Calendar = SpecBase.calendar

  def vatAccountSummary: AccountSummaryData = SpecBase.vatAccountSummary

  def defaultVatData: VatData = SpecBase.defaultVatData

}

object SpecBase {
  //Sample data
  val defaultDirectDebit: DirectDebit = DirectDebit(false, None)
  val periodWithOutstandingReturn: CalendarPeriod = CalendarPeriod(
    LocalDateTime.now.minusMonths(1).toLocalDate, LocalDateTime.now.toLocalDate, None, false
  )
  val periodWithCompletedReturn: CalendarPeriod = CalendarPeriod(
    LocalDateTime.now.minusMonths(1).toLocalDate, LocalDateTime.now.toLocalDate, Some(LocalDateTime.now.minusDays(1).toLocalDate), false
  )

  lazy val calendar: Calendar = Calendar(
    filingFrequency = Monthly,
    directDebit = InactiveDirectDebit
  )
  val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
  val defaultVatData: VatData = VatData(vatAccountSummary, Some(calendar), Some(0))
}