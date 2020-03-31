/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.payments.{NextUrl, PayConnector}
import controllers.actions._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.Helpers._
import services.VatService

import scala.concurrent.Future

class PaymentStartControllerSpec extends ControllerSpecBase {

  private val mockVatService: VatService = mock[VatService]
  private val mockPayConnector: PayConnector = mock[PayConnector]

  private val testAccountBalance = AccountBalance(Some(0.0))
  private val testVatData = VatData(AccountSummaryData(Some(testAccountBalance), None, Seq.empty), calendar = None, Some(0))
  private val testVatDataNoAccountBalance = VatData(AccountSummaryData(None, None, Seq.empty), calendar = None, Some(0))
  private val testPayUrl = "https://www.tax.service.gov.uk/pay/12345/choose-a-way-to-pay"


  override def moduleOverrides: Seq[Binding[_]] =
    Seq(
      bind[PayConnector].toInstance(mockPayConnector),
      bind[VatService].toInstance(mockVatService)
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatService)
    reset(mockPayConnector)
    when(mockPayConnector.vatPayLink(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(NextUrl(testPayUrl)))
  }

  def mockFetchVatModel(testModel: Future[Either[VatAccountFailure, Option[VatData]]]): Unit =
    when(mockVatService.fetchVatModel(any())(any())).thenReturn(testModel)

  def mockFetchVatModel(testModel: Either[VatAccountFailure, Option[VatData]]): Unit =
    mockFetchVatModel(Future.successful(testModel))

  def SUT: PaymentStartController = inject[PaymentStartController]

  "Payment Controller" must {
    "return See Other and a NextUrl for a GET with the correct user information available" in {
      mockFetchVatModel(Right(Some(testVatData)))

      val result: Future[Result] = SUT.makeAPayment(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(testPayUrl)
    }

    "return Bad Request and the error page when the call to the backend fails" in {
      mockFetchVatModel(Future.failed(new Throwable()))

      val result: Future[Result] = SUT.makeAPayment(fakeRequest)

      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }

    "return Bad Request and the error page when the user has no account balance" in {
      mockFetchVatModel(Right(Some(testVatDataNoAccountBalance)))

      val result: Future[Result] = SUT.makeAPayment(fakeRequest)

      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }

    "return Bad Request and the error page when the user has erroneous vat data " in {
      mockFetchVatModel(Left(VatGenericError))

      val result: Future[Result] = SUT.makeAPayment(fakeRequest)

      contentType(result) mustBe Some("text/html")
      status(result) mustBe BAD_REQUEST
    }
  }

}
