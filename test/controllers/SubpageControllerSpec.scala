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
import controllers.helpers.SidebarHelper
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
import views.html.subpage_aggregated

import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

class SubpageControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with ViewSpecBase {

  //TODO: Needs VatModel
  val vatModel = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val currentUrl = ""
  val mockAccountSummaryHelper = mock[AccountSummaryHelper]
  val mockHelper = mock[Helper]
  val mockSidebarHelper = mock[SidebarHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any())).thenReturn(Future.successful(vatModel))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new SubpageController(frontendAppConfig, messagesApi, FakeAuthAction, FakeServiceInfoAction, mockHelper, mockAccountSummaryHelper,
      mockSidebarHelper)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)

  def authenticatedRequest = AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(true), VatNoEnrolment())

  def requestWithEnrolment(activated: Boolean): ServiceInfoRequest[AnyContent] = {
    ServiceInfoRequest[AnyContent](AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment()), HtmlFormat.empty)
  }

  val fakeRequestWithEnrolments = requestWithEnrolment(activated = true)

  val testAccountSummary = Html("<p> Account summary goes here </p>")
  val testSidebar = views.html.partials.sidebar_links(vrnEnrolment(true),frontendAppConfig,
    views.html.partials.sidebar.filing_calendar_missing(frontendAppConfig, vrnEnrolment(true))(fakeRequestWithEnrolments.request.request, messages))(fakeRequestWithEnrolments.request.request, messages)
  val testVatVarPartial = views.html.partials.account_summary.vat.vat_var.vat_var_activation(currentUrl,frontendAppConfig)(messages, fakeRequestWithEnrolments.request)
  when(mockAccountSummaryHelper.getVatVarsActivationView(Matchers.any())(Matchers.any())).thenReturn(
    Future.successful(testVatVarPartial))
  when(mockAccountSummaryHelper.renderAccountSummaryView(Matchers.any(),Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(testAccountSummary))
  when(mockSidebarHelper.buildSideBar(Matchers.any())(Matchers.any())).thenReturn(Future(testSidebar))

  def viewAggregatedSubpageAsString(balanceInformation: String = "") =
    subpage_aggregated(frontendAppConfig,testAccountSummary,testSidebar, testVatVarPartial,vrnEnrolment(true))(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequestWithEnrolments.request.request,messages).toString
  "Subpage Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequestWithEnrolments)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAggregatedSubpageAsString(balanceInformation = "No balance information to display")
    }
  }

}
