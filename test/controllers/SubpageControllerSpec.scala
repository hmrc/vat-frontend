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

import connectors.models.{AccountSummaryData, VatModel}
import controllers.actions._
import models._
import models.requests.{AuthenticatedRequest, ServiceInfoRequest}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import views.html.subpage2

import scala.concurrent.Future
import scala.util.Success

class SubpageControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with ViewSpecBase {

  //TODO: Needs VatModel
  val vatModel = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val currentUrl = ""
  val mockAccountSummaryHelper = mock[AccountSummaryHelper]
  val mockHelper = mock[Helper]
  when(mockAccountSummaryHelper.getVatModel(Matchers.any())).thenReturn(Future.successful(vatModel))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new SubpageController(frontendAppConfig, messagesApi, FakeAuthAction, FakeServiceInfoAction, mockHelper, mockAccountSummaryHelper)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)

  def authenticatedRequest = AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(true), VatNoEnrolment())

  def requestWithEnrolment(activated: Boolean): ServiceInfoRequest[AnyContent] = {
    ServiceInfoRequest[AnyContent](AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment()), HtmlFormat.empty)
  }

  val fakeRequestWithEnrolments = requestWithEnrolment(activated = true)

  def viewAsString(balanceInformation: String = "") =
    subpage2(vatModel, currentUrl, frontendAppConfig, mockHelper)(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequest, messages, authenticatedRequest).toString

  "Subpage Controller" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequestWithEnrolments)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(balanceInformation = "No balance information to display")
    }
  }

}
