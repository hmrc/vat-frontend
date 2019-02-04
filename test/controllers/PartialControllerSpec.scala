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
import services.VatServiceInterface
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import views.html.partial

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {

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
    messagesApi, FakeAuthAction, FakeServiceInfoAction, mockAccountSummaryHelper, frontendAppConfig, vatService)

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
      val result: Future[Result] = customController().getCard(fakeRequest)
      contentType(result) mustBe Some("application/json")
      status(result) mustBe OK
    }

    "return an error status when asked to get a card and the call to the backend fails" in {
      val result: Future[Result] = brokenController.getCard(fakeRequest)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return a card with the text 'You have nothing to pay' when the balance is 0" in {
      val result =customController(VatData(AccountSummaryData(Some(AccountBalance(Some(0.0))), None), calendar = None)).getCard(fakeRequest)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.obj(
        "title" -> "VAT",
        "description" -> "You have nothing to pay",
        "referenceNumber" -> "vrn",
        "primaryLink" -> Json.obj(
          "id" -> "vat-account-details-card-link",
          "title"->"VAT",
          "href" -> "http://localhost:9732/business-account/vat",
          "ga" -> "link - click:Your business taxes cards:More VAT details",
          "external" -> false
        ),
        "messageReferenceKey" -> "card.vat.your_vrn_",
        "paymentsPartial" -> "<p> Payments - WORK IN PROGRESS</p>",
        "returnsPartial" -> "<p> Returns - WORK IN PROGRESS</p>"
      )
    }

    "return a card with the text 'You owe £x.yz' when the balance is x.yz" in {
      val result =customController(VatData(AccountSummaryData(Some(AccountBalance(Some(1.34))), None), calendar = None)).getCard(fakeRequest)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.obj(
        "title" -> "VAT",
        "description" -> "You owe £1.34",
        "referenceNumber" -> "vrn",
        "primaryLink" -> Json.obj(
          "id" -> "vat-account-details-card-link",
          "title"->"VAT",
          "href" -> "http://localhost:9732/business-account/vat",
          "ga" -> "link - click:Your business taxes cards:More VAT details",
          "external" -> false
        ),
        "messageReferenceKey" -> "card.vat.your_vrn_",
        "paymentsPartial" -> "<p> Payments - WORK IN PROGRESS</p>",
        "returnsPartial" -> "<p> Returns - WORK IN PROGRESS</p>"
      )
    }

    "return a card with the text 'You are £x.yz in credit' when the balance is x.yz" in {
      val result =customController(VatData(AccountSummaryData(Some(AccountBalance(Some(-1.63))), None), calendar = None)).getCard(fakeRequest)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.obj(
        "title" -> "VAT",
        "description" -> "You are £1.63 in credit",
        "referenceNumber" -> "vrn",
        "primaryLink" -> Json.obj(
          "id" -> "vat-account-details-card-link",
          "title"->"VAT",
          "href" -> "http://localhost:9732/business-account/vat",
          "ga" -> "link - click:Your business taxes cards:More VAT details",
          "external" -> false
        ),
        "messageReferenceKey" -> "card.vat.your_vrn_",
        "paymentsPartial" -> "<p> Payments - WORK IN PROGRESS</p>",
        "returnsPartial" -> "<p> Returns - WORK IN PROGRESS</p>"
      )
    }

  }


}
