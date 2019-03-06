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

import connectors.models._
import connectors.models.designatorydetails.DesignatoryDetailsCollection
import controllers.actions._
import controllers.helpers.AccountSummaryHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{VatCardBuilderService, VatPartialBuilder, VatServiceInterface}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import views.html.partial

import uk.gov.hmrc.http.Upstream5xxResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {

  lazy val vatPartialBuilder: VatPartialBuilder = mock[VatPartialBuilder]
  lazy val vatCardBuilderService: VatCardBuilderService = mock[VatCardBuilderService]

  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(Html(""))

  class VatServiceMethods {
    def designatoryDetails(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[DesignatoryDetailsCollection]] = ???
    def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = ???
    def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[Calendar]] = ???
  }

  class TestVatService(testModel: VatData) extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData] =
      Future(testModel)
  }

  class BrokenVatService extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData] =
      Future.failed(new Throwable())
  }

  def buildController(vatService: VatServiceInterface) = new PartialController(
    messagesApi, FakeAuthAction, FakeServiceInfoAction, mockAccountSummaryHelper, frontendAppConfig, vatService, vatPartialBuilder, vatCardBuilderService)

  def customController(testModel: VatData = VatData(AccountSummaryData(Some(AccountBalance(Some(0.0))), None), calendar = None)) = {
    buildController(new TestVatService(testModel))
  }

  def brokenController = buildController(new BrokenVatService)

  def viewAsString(): String = partial(Vrn("vrn"),frontendAppConfig, Html(""))(fakeRequest, messages).toString

  "Partial Controller" must {

    "return OK and the correct view for a GET" in {
      val result = customController().onPageLoad(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return 200 in json format when asked to get a card and the call to the backend succeeds" in {
      when(vatCardBuilderService.buildVatCard()(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Card(
        "title",
        "descripton",
        "reference")))
      val result: Future[Result] = customController().getCard(fakeRequest)
      contentType(result) mustBe Some("application/json")
      status(result) mustBe OK
    }

    "return an error status when asked to get a card and the call to the backend fails" ignore {
      when(vatCardBuilderService.buildVatCard()(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", 500, 500)))
      val result: Future[Result] = customController().getCard(fakeRequest)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

  }

}
