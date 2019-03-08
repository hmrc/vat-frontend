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
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{VatCardBuilderService, VatServiceInterface}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{EnrolmentsStoreService, VatServiceInterface, VatVarPartialBuilder}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import views.html.partial

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {

  val vatCardBuilderService: VatCardBuilderService = mock[VatCardBuilderService]
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]

  class VatServiceMethods {
    def designatoryDetails(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[DesignatoryDetailsCollection]] = ???
    def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = ???
    def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[Calendar]] = ???
  }

  class TestVatService extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData] =
      Future(VatData(AccountSummaryData(Some(AccountBalance(Some(0.0))), None), calendar = None))
  }

  def buildController = new PartialController(
    messagesApi, FakeAuthActionActiveVatVar, mockAccountSummaryHelper, frontendAppConfig, new TestVatService, vatCardBuilderService, VatVarBuilderReturnsPartial)


  val VatVarBuilderReturnsNone = new VatVarPartialBuilder {
    override def getPartialForSubpage(vatVarEnrolment: VatEnrolment, vatDecEnrolment: VatDecEnrolment)
                                     (implicit request: Request[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future(None)
    override def getPartialForCard(vatVarEnrolment: VatEnrolment, vatDecEnrolment: VatDecEnrolment)
                                  (implicit request: Request[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future(None)
  }

  val VatVarBuilderReturnsPartial = new VatVarPartialBuilder {
    override def getPartialForSubpage(vatVarEnrolment: VatEnrolment, vatDecEnrolment: VatDecEnrolment)
                                     (implicit request: Request[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future(Some(Html("<p>VatVar partial</p>")))
    override def getPartialForCard(vatVarEnrolment: VatEnrolment, vatDecEnrolment: VatDecEnrolment)
                                  (implicit request: Request[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future(Some(Html("<p>VatVar partial</p>")))
  }

  val VatVarBuilderReturnsFailure = new VatVarPartialBuilder {
    override def getPartialForSubpage(vatVarEnrolment: VatEnrolment, vatDecEnrolment: VatDecEnrolment)
                                     (implicit request: Request[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future.failed(new Throwable("test exception"))
    override def getPartialForCard(vatVarEnrolment: VatEnrolment, vatDecEnrolment: VatDecEnrolment)
                                  (implicit request: Request[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future.failed(new Throwable("test exception"))
  }


  def viewAsString(): String = partial(Vrn("vrn"),frontendAppConfig, Html(""))(fakeRequest, messages).toString

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
        "descripton",
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
