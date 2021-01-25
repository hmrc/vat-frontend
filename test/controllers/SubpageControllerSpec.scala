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

package controllers

import models.{Vrn, _}
import models.requests.{AuthenticatedRequest, ServiceInfoRequest}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import services.local.{AccountSummaryHelper, SidebarHelper}
import services.payment.{PaymentHistoryService, PaymentHistoryServiceInterface}
import services.{VatPartialBuilder, VatService}
import uk.gov.hmrc.http.HeaderCarrier
import views.ViewSpecBase
import views.html.subpage

import scala.concurrent.Future

class SubpageControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with ViewSpecBase {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testAccountSummary: Html = Html("<p> Account summary goes here </p>")
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(testAccountSummary)
  val mockSidebarHelper: SidebarHelper = mock[SidebarHelper]
  val mockVatService: VatService = mock[VatService]
  when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Right(None)))
  val mockVatPartialBuilder: VatPartialBuilder = mock[VatPartialBuilder]
  when(mockVatPartialBuilder.buildVatVarPartial(Matchers.eq(false))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  val mockPaymentHistoryService: PaymentHistoryServiceInterface = mock[PaymentHistoryService]
  when(mockPaymentHistoryService.getPayments(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Right(Nil)))

  override def moduleOverrides = Seq(
    bind[AccountSummaryHelper].toInstance(mockAccountSummaryHelper),
    bind[SidebarHelper].toInstance(mockSidebarHelper),
    bind[VatService].toInstance(mockVatService),
    bind[VatPartialBuilder].toInstance(mockVatPartialBuilder),
    bind[PaymentHistoryServiceInterface].toInstance(mockPaymentHistoryService)
  )

  def controller(): SubpageController = inject[SubpageController]

  def vrnEnrolment(activated: Boolean = true): VatDecEnrolment = VatDecEnrolment(Vrn(testVrn), isActivated = true)

  def authenticatedRequest: AuthenticatedRequest[AnyContent] = AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(true), VatNoEnrolment(), "credId")

  def requestWithEnrolment(activated: Boolean): ServiceInfoRequest[AnyContent] = {
    ServiceInfoRequest[AnyContent](AuthenticatedRequest(FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment(), "credId"), HtmlFormat.empty)
  }

  val fakeRequestWithEnrolments: ServiceInfoRequest[AnyContent] = requestWithEnrolment(activated = true)

  val testSidebar: Html = views.html.partials.sidebar_links(vrnEnrolment(true), frontendAppConfig,
    views.html.partials.sidebar.filing_calendar_missing(frontendAppConfig,
      vrnEnrolment(true))(fakeRequestWithEnrolments.request.request, messages))(fakeRequestWithEnrolments.request.request, messages)
  when(mockSidebarHelper.buildSideBar(Matchers.any())(Matchers.any())).thenReturn(testSidebar)

  def viewAggregatedSubpageAsString(balanceInformation: String = ""): String =
    inject[subpage].apply(frontendAppConfig, testAccountSummary, testSidebar, vrnEnrolment(true), Html(""))(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequestWithEnrolments.request.request, messages).toString

  "Subpage Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequestWithEnrolments)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAggregatedSubpageAsString(balanceInformation = "No balance information to display")
    }

  }

}
