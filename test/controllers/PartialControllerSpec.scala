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

import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import models.{Vrn, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.local.AccountSummaryHelper
import services.payment.PaymentHistoryServiceInterface
import services.{VatCardBuilderService, VatPartialBuilder, VatServiceInterface}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import java.time.OffsetDateTime
import java.time.{LocalDateTime, OffsetDateTime}

import scala.concurrent.Future


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vatCardBuilderService: VatCardBuilderService = mock[VatCardBuilderService]
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  val vatPartialBuilder: VatPartialBuilder = mock[VatPartialBuilder]

  val vatEnrolment: VatDecEnrolment = VatDecEnrolment(Vrn("123456789"), isActivated = true)

  def authenticatedRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(
    request = FakeRequest(), externalId = "", vatDecEnrolment = vatEnrolment, vatVarEnrolment = VatNoEnrolment(), credId = "credId")

  class VatServiceMethods {
    def determineFrequencyFromStaggerCode(staggerCode: String): FilingFrequency = ???

    def vatCalendar(vatEnrolment: VatEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Option[CalendarDerivedInformation]] = ???
  }

  class TestVatService extends VatServiceMethods with VatServiceInterface {
    override def fetchVatModel(vatEnrolmentOpt: VatDecEnrolment)(implicit headerCarrier: HeaderCarrier): Future[Either[VatAccountFailure, Option[VatData]]] =
      Future.successful(Right(Some(VatData(AccountSummaryData(Some(AccountBalance(Some(0.0))), None), calendar = None, Some(0)))))
  }

  class TestPaymentHistory extends PaymentHistoryServiceInterface {
    def getPayments(enrolment: Option[VatEnrolment])(implicit hc: HeaderCarrier): Future[Either[PaymentRecordFailure.type, List[PaymentRecord]]] = Future.successful(Right(List.empty))

    def getDateTime: LocalDateTime = LocalDateTime.now()
  }

  override def moduleOverrides: Seq[Binding[_]] = Seq(
    bind[VatCardBuilderService].toInstance(vatCardBuilderService)
  )

  def buildController: PartialController = inject[PartialController]

  when(vatPartialBuilder.buildVatVarPartial(any())(any(), any(), any())).thenReturn(
    Future.successful(Some(Html("<p>VatVar partial</p>")))
  )

  "Partial Controller" must {

    "return 200 in json format when asked to get a card and the call to the backend succeeds" in {
      when(vatCardBuilderService.buildVatCard()(any(), any(), any())).thenReturn(Future.successful(Card(
        "title",
        "description",
        "reference")))
      val result: Future[Result] = buildController.getCard(fakeRequest)
      contentType(result) mustBe Some("application/json")
      status(result) mustBe OK
    }

    "return an error status when asked to get a card and the call to the backend fails" in {
      when(vatCardBuilderService.buildVatCard()(any(),
        any(), any())).thenReturn(Future.failed(UpstreamErrorResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
      val result: Future[Result] = buildController.getCard(fakeRequest)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }

}
