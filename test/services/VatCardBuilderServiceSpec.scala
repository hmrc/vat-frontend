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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.models._
import controllers.actions.ServiceInfoAction
import controllers.helpers.AccountSummaryHelper
import models._
import models.payment.{PaymentRecord, Successful}
import models.requests.AuthenticatedRequest
import org.joda.time.DateTime
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object VatPartialBuilderTestWithVatVar extends VatPartialBuilder {
  override def buildReturnsPartial(vatData: VatData, enrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Returns partial")
  override def buildPaymentsPartial(vatData: VatData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Payments partial")
  override def buildVatVarPartial(forCard: Boolean)(implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future(Some(Html("Vat Vat for Card Partial")))
}

object VatPartialBuilderTestWithoutVatVar extends VatPartialBuilder {
  override def buildReturnsPartial(vatData: VatData, enrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Returns partial")
  override def buildPaymentsPartial(vatData: VatData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Payments partial")
  override def buildVatVarPartial(forCard: Boolean)(implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] = Future(None)
}


class VatCardBuilderServiceSpec extends SpecBase with ScalaFutures with MockitoSugar {

  class VatCardBuilderServiceTest(messagesApi: MessagesApi,
                                  testVatPartialBuilder: VatPartialBuilder,
                                  testServiceInfo: ServiceInfoAction,
                                  testAccountSummaryHelper: AccountSummaryHelper,
                                  testAppConfig: FrontendAppConfig,
                                  testVatService: VatServiceInterface,
                                  testPaymentHistoryService: PaymentHistoryServiceInterface
                                 ) extends VatCardBuilderServiceImpl(messagesApi, testVatPartialBuilder, testServiceInfo,
    testAccountSummaryHelper, testAppConfig, testVatService, testPaymentHistoryService)


  trait LocalSetup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    lazy val vatEnrolment: VatDecEnrolment =  VatDecEnrolment(Vrn("123456789"), isActivated = true)
    def authenticatedRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(request = FakeRequest(), externalId = "", vatDecEnrolment = vatEnrolment, vatVarEnrolment = VatNoEnrolment())

    val testVatPartialBuilder: VatPartialBuilder

    lazy val testServiceInfo: ServiceInfoAction = mock[ServiceInfoAction]
    lazy val testAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
    lazy val testAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
    lazy val testVatService: VatServiceInterface = mock[VatServiceInterface]
    lazy val testPaymentHistoryService: PaymentHistoryServiceInterface = mock[PaymentHistoryServiceInterface]

    lazy val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
    lazy val vatCalendarData: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(true, None), None, Seq()))
    lazy val vatCalendar: Option[Calendar] = Some(Calendar( filingFrequency = Monthly, directDebit = InactiveDirectDebit))
    lazy val vatData: VatAccountData = VatData(vatAccountSummary, vatCalendar)

    def testCard(payments: List[PaymentRecord] = Nil): Card = Card(
      title = "VAT",
      referenceNumber = "123456789",
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          ga = "link - click:VAT cards:More VAT details",
          dataSso = None,
          external = false
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = Some("Payments partial"),
      returnsPartial = Some("Returns partial"),
      vatVarPartial = None,
      paymentHistory = payments
    )

    lazy val testCardWithVatVarPartial: Card = Card(
      title = "VAT",
      referenceNumber = "123456789",
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          ga = "link - click:VAT cards:More VAT details",
          dataSso = None,
          external = false
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = Some("Payments partial"),
      returnsPartial = Some("Returns partial"),
      vatVarPartial = Some("Vat Vat for Card Partial"),
      paymentHistory = Nil
    )

    lazy val testCardNoData: Card = Card(
      title = "VAT",
      referenceNumber = "123456789",
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          ga = "link - click:VAT cards:More VAT details",
          dataSso = None,
          external = false
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = Some("\n\n<p role = \"text\">There is no balance information to display.</p>\n"),
      returnsPartial = Some(""),
      vatVarPartial = None
    )

    lazy val service: VatCardBuilderServiceTest = new VatCardBuilderServiceTest(messagesApi, testVatPartialBuilder, testServiceInfo,
      testAccountSummaryHelper, testAppConfig, testVatService, testPaymentHistoryService)
    val date = new DateTime("2018-10-20T08:00:00.000").toLocalDate

    when(testAppConfig.getUrl("mainPage")).thenReturn("http://someTestUrl")
    when(testPaymentHistoryService.getDateTime).thenReturn(date)
    when(testPaymentHistoryService.getPayments(Some(vatEnrolment))).thenReturn(Future.successful(Nil))
  }


  "Calling VatCardBuilderService.buildVatCard" should {

    "return a card with No Payments information when getting VatNoData" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(VatNoData))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardNoData
    }

    "return a card with Payment information when getting Vat Data" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(vatData))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard()
    }

    "return a card with payment history" in new LocalSetup {

      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(VatNoData))

      val payments = List(PaymentRecord(
        reference = "reference number",
        amountInPence = 100,
        status = Successful,
        createdOn = "2018-10-20T08:00:00.000",
        taxType = "tax type"
      ))

      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(vatData))
      when(testPaymentHistoryService.getDateTime).thenReturn(date)
      when(testPaymentHistoryService.getPayments(Some(vatEnrolment))).thenReturn(Future.successful(payments))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard(payments)
    }

    "return a card with a vat var partial when one is provided" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithVatVar
      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(vatData))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardWithVatVarPartial
    }
  }

}