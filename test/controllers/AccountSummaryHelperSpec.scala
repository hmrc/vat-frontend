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

package controllers

import base.SpecBase
import connectors.models.{AccountSummaryData, VatModel}
import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import views.html.subpage2

import scala.concurrent.Future
import scala.util.Success

class AccountSummaryHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures with SpecBase {

  //TODO: Needs AccountSummaryData
  val accountSummary = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  val mockHelper = mock[Helper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any())).thenReturn(Future.successful(accountSummary))

  val mockVatService: VatService = mock[VatService]

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockVatService, messagesApi)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)
  def requestWithEnrolment(activated: Boolean): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", Some(vrnEnrolment(activated)), None)
  }

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(activated = true)

  def viewAsString(balanceInformation: String = ""): String =
    subpage2(accountSummary, "", frontendAppConfig, mockHelper)(HtmlFormat.empty)(fakeRequestWithEnrolments, messages, requestWithEnrolment(true)).toString

  "getAccountSummaryView" when {

  }

}
