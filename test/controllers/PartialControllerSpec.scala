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
import controllers.actions._
import controllers.helpers.AccountSummaryHelper
import models._
import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.payment.PaymentHistoryServiceInterface
import services.{VatCardBuilderService, VatPartialBuilder, VatServiceInterface}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import views.html.partial

import scala.concurrent.Future


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vatCardBuilderService: VatCardBuilderService = mock[VatCardBuilderService]
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  val vatPartialBuilder: VatPartialBuilder = mock[VatPartialBuilder]

  lazy val vatEnrolment: VatDecEnrolment =  VatDecEnrolment(Vrn("123456789"), isActivated = true)
  def authenticatedRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(request = FakeRequest(), externalId = "", vatDecEnrolment = vatEnrolment, vatVarEnrolment = VatNoEnrolment(), credId = "credId")

  class VatServiceMethods {
    def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = ???
    def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[Calendar]] = ???
  }

  class TestVatService extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: VatDecEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Either[VatAccountFailure, Option[VatData]]] =
      Future.successful(Right(Some(VatData(AccountSummaryData(Some(AccountBalance(Some(0.0))), None), calendar = None))))
  }

  class TestPaymentHistory extends PaymentHistoryServiceInterface {
    def getPayments(enrolment: Option[VatEnrolment])(implicit hc: HeaderCarrier): Future[Either[PaymentRecordFailure.type, List[PaymentRecord]]] = Future.successful(Right(List.empty))
    def getDateTime: DateTime = DateTime.now()
  }

  def buildController = new PartialController(
    messagesApi,
    FakeAuthActionActiveVatVar,
    mockAccountSummaryHelper,
    frontendAppConfig,
    new TestVatService,
    vatCardBuilderService,
    vatPartialBuilder,
    new TestPaymentHistory
  )

  when(vatPartialBuilder.buildVatVarPartial(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(Html("<p>VatVar partial</p>"))))

  def viewAsString(): String = partial(Vrn("vrn"),frontendAppConfig, Html(""), Html("<p>VatVar partial</p>"))(fakeRequest, messages).toString

  "Partial Controller" must {

    "return OK and the correct view for a GET" in {
      val result = buildController.onPageLoad(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return 200 in json format when asked to get a card and the call to the backend succeeds" in {
      when(vatCardBuilderService.buildVatCard()(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Card(
        "title",
        "description",
        "reference")))
      val result: Future[Result] = buildController.getCard(fakeRequest)
      contentType(result) mustBe Some("application/json")
      status(result) mustBe OK
    }

    "return an error status when asked to get a card and the call to the backend fails" in {
      when(vatCardBuilderService.buildVatCard()(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", 500, 500)))
      val result: Future[Result] = buildController.getCard(fakeRequest)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }

}
