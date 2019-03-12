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

package controllers

import connectors.models.VatNoData
import controllers.actions._
import controllers.helpers.{AccountSummaryHelper, SidebarHelper}
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
import services.{VatPartialBuilder, VatService}
import services.VatService
import services.payment.{PaymentHistoryService, PaymentHistoryServiceInterface}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import views.ViewSpecBase
import views.html.subpage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubpageControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with ViewSpecBase {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testAccountSummary = Html("<p> Account summary goes here </p>")
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(testAccountSummary)
  val mockSidebarHelper: SidebarHelper = mock[SidebarHelper]
  val mockVatService:VatService = mock[VatService]
  when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future(VatNoData))
  val vatPartialBuilder: VatPartialBuilder = mock[VatPartialBuilder]
  when(vatPartialBuilder.buildVatVarPartial(Matchers.eq(false))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  val mockPaymentHistoryService: PaymentHistoryServiceInterface = mock[PaymentHistoryService]
  when(mockPaymentHistoryService.getPayments(Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(Future.successful(Nil))

  def controller() =
    new SubpageController(frontendAppConfig, messagesApi, FakeAuthActionNoVatVar, FakeServiceInfoAction, mockAccountSummaryHelper,
      mockSidebarHelper, mockVatService, vatPartialBuilder, mockPaymentHistoryService)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)

  def authenticatedRequest = AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(true), VatNoEnrolment())

  def requestWithEnrolment(activated: Boolean): ServiceInfoRequest[AnyContent] = {
    ServiceInfoRequest[AnyContent](AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment()), HtmlFormat.empty)
  }

  val fakeRequestWithEnrolments: ServiceInfoRequest[AnyContent] = requestWithEnrolment(activated = true)

  val testSidebar: Html = views.html.partials.sidebar_links(vrnEnrolment(true),frontendAppConfig,
    views.html.partials.sidebar.filing_calendar_missing(frontendAppConfig,
      vrnEnrolment(true))(fakeRequestWithEnrolments.request.request, messages))(fakeRequestWithEnrolments.request.request, messages)
  when(mockSidebarHelper.buildSideBar(Matchers.any())(Matchers.any())).thenReturn(testSidebar)

  def viewAggregatedSubpageAsString(balanceInformation: String = ""):String =
    subpage(frontendAppConfig,testAccountSummary,testSidebar,vrnEnrolment(true),Html(""), Nil)(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequestWithEnrolments.request.request,messages).toString
  "Subpage Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequestWithEnrolments)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAggregatedSubpageAsString(balanceInformation = "No balance information to display")
    }

  }

}
