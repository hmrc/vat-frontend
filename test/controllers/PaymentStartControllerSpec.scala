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
import connectors.payments.{NextUrl, PayConnector}
import controllers.actions._
import models._
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers._
import services.VatServiceInterface
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PaymentStartControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val testAccountBalance = AccountBalance(Some(0.0))
  private val testOpenPeriod = OpenPeriod(LocalDate.now())
  private val testVatData = VatData(AccountSummaryData(Some(testAccountBalance), None, Seq(testOpenPeriod)), calendar = None)
  private val testVatDataNoAccountBalance = VatData(AccountSummaryData(None, None, Seq(testOpenPeriod)), calendar = None)
  private val testVatDataNoOpenPeriods = VatData(AccountSummaryData(Some(testAccountBalance), None), calendar = None)
  private val testPayUrl = "https://www.tax.service.gov.uk/pay/12345/choose-a-way-to-pay"

  private val mockPayConnector: PayConnector = mock[PayConnector]
  when(mockPayConnector.vatPayLink(Matchers.any())(Matchers.any())).thenReturn(Future.successful(NextUrl(testPayUrl)))

  class VatServiceMethods {
    def designatoryDetails(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[DesignatoryDetailsCollection]] = ???
    def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = ???
    def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[Calendar]] = ???
  }

  class TestVatService(testModel: VatAccountData) extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData] =
      Future(testModel)
  }

  class BrokenVatService extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: Option[VatDecEnrolment])(implicit headerCarrier: HeaderCarrier): Future[VatAccountData] =
      Future.failed(new Throwable())
  }

  def buildController(vatService: VatServiceInterface) = new PaymentStartController(
    frontendAppConfig, mockPayConnector, FakeAuthAction, vatService, messagesApi)

  def customController(testModel: VatAccountData = testVatData): PaymentStartController = {
    buildController(new TestVatService(testModel))
  }

  def brokenController: PaymentStartController = buildController(new BrokenVatService)

  "Partial Controller" must {

    "return See Other and a NextUrl for a GET with the correct user information available" in {
      val result: Future[Result] = customController().makeAPayment(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(testPayUrl)
    }

    "return Bad Request and the error page when the call to the backend fails" in {
      val result: Future[Result] = brokenController.makeAPayment(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }

    "return Bad Request and the error page when the user has no account balance" in {
      val result: Future[Result] = customController(testVatDataNoAccountBalance).makeAPayment(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }

    "return Bad Request and the error page when the user has no open periods" in {
      val result: Future[Result] = customController(testVatDataNoOpenPeriods).makeAPayment(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }

    "return Bad Request and the error page when the user has erroneous vat data " in {
      val result: Future[Result] = customController(VatGenericError).makeAPayment(fakeRequest)
      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }
  }
}
