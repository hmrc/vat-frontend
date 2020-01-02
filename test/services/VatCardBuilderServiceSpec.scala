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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.models._
import controllers.actions.ServiceInfoAction
import controllers.helpers.AccountSummaryHelper
import models._
import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import org.joda.time.DateTime
import org.mockito.Matchers
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

object VatPartialBuilderTestWithVatVar extends VatPartialBuilder {
  override def buildReturnsPartial(vatData: VatData, enrolment: VatEnrolment)(
    implicit request: AuthenticatedRequest[_], messages: Messages
  ): Html = Html("Returns partial")
  override def buildPaymentsPartial(vatData: VatData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Payments partial")
  override def buildVatVarPartial(forCard: Boolean)(
    implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier
  ): Future[Option[Html]] = Future.successful(Some(Html("Vat Vat for Card Partial")))
}

object VatPartialBuilderTestWithoutVatVar extends VatPartialBuilder {
  override def buildReturnsPartial(vatData: VatData, enrolment: VatEnrolment)(
    implicit request: AuthenticatedRequest[_], messages: Messages
  ): Html = Html("Returns partial")
  override def buildPaymentsPartial(vatData: VatData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Payments partial")
  override def buildVatVarPartial(forCard: Boolean)(
    implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier
  ): Future[Option[Html]] = Future.successful(None)
}


class VatCardBuilderServiceSpec extends SpecBase with ScalaFutures with MockitoSugar {

  class VatCardBuilderServiceTest(messagesApi: MessagesApi,
                                  testVatPartialBuilder: VatPartialBuilder,
                                  testServiceInfo: ServiceInfoAction,
                                  testAccountSummaryHelper: AccountSummaryHelper,
                                  testAppConfig: FrontendAppConfig,
                                  testVatService: VatServiceInterface,
                                  testPaymentHistoryService: PaymentHistoryServiceInterface,
                                  testLinkProviderService: LinkProviderService
                                 ) extends VatCardBuilderServiceImpl(messagesApi, testVatPartialBuilder, testServiceInfo,
    testAccountSummaryHelper, testAppConfig, testVatService, testPaymentHistoryService, testLinkProviderService)


  trait LocalSetup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    lazy val vrn: Vrn = Vrn("123456789")
    lazy val vatEnrolment: VatDecEnrolment =  VatDecEnrolment(vrn, isActivated = true)
    def authenticatedRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(
      request = FakeRequest(), externalId = "", vatDecEnrolment = vatEnrolment, vatVarEnrolment = VatNoEnrolment(), credId = "credId")

    val testVatPartialBuilder: VatPartialBuilder

    lazy val testServiceInfo: ServiceInfoAction = mock[ServiceInfoAction]
    lazy val testAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
    lazy val testAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
    lazy val testVatService: VatServiceInterface = mock[VatServiceInterface]
    lazy val testPaymentHistoryService: PaymentHistoryServiceInterface = mock[PaymentHistoryServiceInterface]
    lazy val testLinkProviderService: LinkProviderService = mock[LinkProviderService]

    lazy val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
    lazy val vatCalendarData: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(true, None), None, Seq()))
    lazy val vatCalendar: Option[Calendar] = Some(Calendar( filingFrequency = Monthly, directDebit = InactiveDirectDebit))
    lazy val vatData: VatData = VatData(vatAccountSummary, vatCalendar, Some(0))

    def testCard(maybePayments: Either[PaymentRecordFailure.type, List[PaymentRecord]] = Right(Nil)): Card = Card(
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
      paymentHistory = maybePayments,
      paymentSectionAdditionalLinks = Some(List(makePaymentLink))
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
      paymentHistory = Right(Nil),
      paymentSectionAdditionalLinks = Some(List(makePaymentLink))
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
      returnsPartial = Some(
        "\n<a id=\"complete-vat-return\" href=\"http://localhost:8080/portal/vat-file/trader/123456789/return?lang=eng\"\n   target=\"_blank\" rel=\"external noopener\"\n   data-journey-click=\"link - click:VAT cards:Complete VAT Return\">\n   Complete VAT Return\n</a>\n"
      ),
      vatVarPartial = None,
      paymentSectionAdditionalLinks = None
    )

    val makePaymentLink = Link(
      id = "vat-make-payment-link",
      title = "Make a VAT payment",
      href = "http://localhost:9732/business-account/vat/make-a-payment",
      ga = "link - click:VAT cards:Make a VAT payment"
    )

    lazy val service: VatCardBuilderServiceTest = new VatCardBuilderServiceTest(messagesApi, testVatPartialBuilder, testServiceInfo,
      testAccountSummaryHelper, testAppConfig, testVatService, testPaymentHistoryService, testLinkProviderService)
    val date = new DateTime("2018-10-20T08:00:00.000")

    when(testAppConfig.getUrl("mainPage")).thenReturn("http://someTestUrl")
    when(testAppConfig.getPortalUrl("vatFileAReturn")(Some(vatEnrolment))(authenticatedRequest)).thenReturn(s"http://localhost:8080/portal/vat-file/trader/$vrn/return?lang=eng")
    when(testPaymentHistoryService.getDateTime).thenReturn(date)
    when(testPaymentHistoryService.getPayments(Some(vatEnrolment))).thenReturn(Future.successful(Right(Nil)))
    when(testLinkProviderService.determinePaymentAdditionalLinks(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Some(List(makePaymentLink)))
  }


  "Calling VatCardBuilderService.buildVatCard" should {

    "return a card with No Payments information when getting VatNoData" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Right(None)))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardNoData
    }

    "return a card with Payment information when getting Vat Data" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Right(Some(vatData))))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard()
    }

    "throw an exception when getting Vat Not Activated" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Left(VatUnactivated)))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.failed.futureValue mustBe a[Exception]
    }

    "throw an exception when getting Vat Empty" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Left(VatEmpty)))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.failed.futureValue mustBe a[Exception]

    }

    "throw an exception when getting Vat Generic Error" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Left(VatGenericError)))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.failed.futureValue mustBe a[Exception]
    }

    "return a card with payment history" in new LocalSetup {

      val testVatPartialBuilder = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Right(None)))

      val payments = Right(List(PaymentRecord(
        reference = "reference number",
        amountInPence = 100,
        createdOn = new DateTime("2018-10-20T08:00:00.000"),
        taxType = "tax type"
      )))

      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Right(Some(vatData))))
      when(testPaymentHistoryService.getDateTime).thenReturn(date)
      when(testPaymentHistoryService.getPayments(Some(vatEnrolment))).thenReturn(Future.successful(payments))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard(payments)
    }

    "return a card with a vat var partial when one is provided" in new LocalSetup {
      val testVatPartialBuilder = VatPartialBuilderTestWithVatVar
      when(testVatService.fetchVatModel(vatEnrolment)).thenReturn(Future.successful(Right(Some(vatData))))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardWithVatVarPartial
    }
  }

}
