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

import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import views.html.subpage

import scala.concurrent.Future

class AccountSummaryHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures {


  val accountSummary = Html("Account Summary")
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any())).thenReturn(Future.successful(accountSummary))

  val mockVatService: VatService = mock[VatService]

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockVatService, messagesApi)


  def vrnEnrolment(activated: Boolean = true) =  VatEnrolment(Vrn("vrn"), isActivated = true)
  def requestWithEnrolment(activated: Boolean): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated))
  }

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(activated = true)

  def viewAsString(balanceInformation: String = ""): String =
    subpage(frontendAppConfig, vrnEnrolment(), accountSummary)(HtmlFormat.empty)(fakeRequestWithEnrolments, messages).toString

  "getAccountSummaryView" when {

  }

}
