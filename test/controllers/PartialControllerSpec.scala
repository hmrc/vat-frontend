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

import connectors.models._
import controllers.actions._
import controllers.helpers.AccountSummaryHelper
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatNoEnrolment}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.html.partial

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {

  //TODO: Needs VatModel
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(Html(""))
  val fakeSummary = Html("<p>This is the account summary</p>")
  val fakeVatVarInfo = Html("<p>This is the vat var info</p>")

  val testVatData: VatData = VatData(AccountSummaryData(None, None), calendar = None)

  val mockVatService = mock[VatService]
  when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future(testVatData))

  def controller() = new PartialController(
    messagesApi,
    FakeAuthAction,
    FakeServiceInfoAction,
    mockAccountSummaryHelper,
    frontendAppConfig,
    mockVatService
  )

  val brokenVatService: VatService = mock[VatService]
  when(brokenVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Throwable()))

  def brokenController() = new PartialController(
    messagesApi,
    FakeAuthAction,
    FakeServiceInfoAction,
    mockAccountSummaryHelper,
    frontendAppConfig,
    brokenVatService
  )

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)

  def requestWithEnrolment(activated: Boolean): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment())
  }

  def viewAsString(): String = partial(Vrn("vrn"),frontendAppConfig, Html(""))(fakeRequest, messages).toString

  "Partial Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return 200 in json format when asked to get a card and the call to the backend succeeds" in {
      val result: Future[Result] = controller().getCard(fakeRequest)
      contentType(result) mustBe Some("application/json")
      status(result) mustBe OK
    }

    "return an error status when asked to get a card and the call to the backend fails" in {
      val result: Future[Result] = brokenController().getCard(fakeRequest)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

  }
}
