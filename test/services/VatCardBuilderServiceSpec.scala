/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.ServiceInfoAction
import models._
import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.local.AccountSummaryHelper
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.http.HeaderCarrier
import views.html.partials.vat.card.panel_info

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object VatPartialBuilderTestWithVatVar extends VatPartialBuilder {

  override def buildReturnsPartial(vatData: VatData, enrolment: VatEnrolment)
                                  (implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Returns partial")

  override def buildPaymentsPartial(vatData: VatData)
                                   (implicit request: AuthenticatedRequest[_], messages: Messages): Html =
    Html("Payments partial")

  override def buildVatVarPartial(forCard: Boolean)(implicit request: AuthenticatedRequest[_],
                                                    messages: Messages,
                                                    headerCarrier: HeaderCarrier): Future[Option[Html]] =
    Future.successful(Some(Html("Vat Vat for Card Partial")))
}

object VatPartialBuilderTestWithoutVatVar extends VatPartialBuilder {
  override def buildReturnsPartial(vatData: VatData, enrolment: VatEnrolment)
                                  (implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Returns partial")

  override def buildPaymentsPartial(vatData: VatData)
                                   (implicit request: AuthenticatedRequest[_], messages: Messages): Html =
    Html("Payments partial")

  override def buildVatVarPartial(forCard: Boolean)(implicit request: AuthenticatedRequest[_],
                                                    messages: Messages,
                                                    headerCarrier: HeaderCarrier): Future[Option[Html]] =
    Future.successful(None)
}

class VatCardBuilderServiceSpec extends SpecBase with ScalaFutures with MockitoSugar {

  class VatCardBuilderServiceTest(
                                   messagesApi: MessagesApi,
                                   testVatPartialBuilder: VatPartialBuilder,
                                   testAppConfig: FrontendAppConfig,
                                   testVatService: VatServiceInterface,
                                   testPaymentHistoryService: PaymentHistoryServiceInterface,
                                   testLinkProviderService: LinkProviderService,
                                   testToday: LocalDate
                                 ) extends VatCardBuilderServiceImpl(
    messagesApi,
    testVatPartialBuilder,
    testAppConfig,
    testVatService,
    testPaymentHistoryService,
    testLinkProviderService
  ) {
    override val today: LocalDate = testToday
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testVrn: String = UUID.randomUUID().toString

  trait LocalSetup {

    lazy val vrn: Vrn = Vrn(testVrn)
    lazy val vatEnrolment: VatDecEnrolment =
      VatDecEnrolment(vrn, isActivated = true)

    val (y, m, d) = (2020, 7, 1)
    val testDate: LocalDate = LocalDate.of(y, m, d)

    def authenticatedRequest: AuthenticatedRequest[AnyContentAsEmpty.type] =
      AuthenticatedRequest(
        request = FakeRequest(),
        externalId = "",
        vatDecEnrolment = vatEnrolment,
        vatVarEnrolment = VatNoEnrolment(),
        credId = "credId"
      )

    val testVatPartialBuilder: VatPartialBuilder

    lazy val testServiceInfo: ServiceInfoAction = mock[ServiceInfoAction]
    lazy val testAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
    lazy val testAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
    lazy val testVatService: VatServiceInterface = mock[VatServiceInterface]
    lazy val testPaymentHistoryService: PaymentHistoryServiceInterface = mock[PaymentHistoryServiceInterface]
    lazy val testLinkProviderService: LinkProviderService = mock[LinkProviderService]

    val balance = 10
    val testAccountBalanceDate: Option[AccountBalance] = Some(AccountBalance(Some(balance)))

    lazy val vatAccountSummary: AccountSummaryData = AccountSummaryData(testAccountBalanceDate, None, Seq())
    lazy val vatCalendarData: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(ddiEligibilityInd = true, None), None, Seq()))
    lazy val vatCalendar: Option[Calendar] = Some(Calendar(filingFrequency = Monthly, directDebit = InactiveDirectDebit))
    lazy val vatData: VatData = VatData(vatAccountSummary, vatCalendar, Some(0))

    def testCard(accountBalance: Option[BigDecimal] = None, maybePayments: Either[PaymentRecordFailure.type, List[PaymentRecord]] = Right(Nil)): Card = {
      Card(
        title = "VAT",
        referenceNumber = testVrn,
        primaryLink = Some(
          Link(
            id = "vat-account-details-card-link",
            title = "VAT",
            href = "http://someTestUrl",
            dataSso = None
          )
        ),
        messageReferenceKey = Some("card.vat.vat_registration_number"),
        panelPartial = Some(panel_info(Some(false), testAppConfig, deferralPeriodOver = true)(messages).toString()),
        paymentsPartial = Some("Payments partial"),
        returnsPartial = Some("Returns partial"),
        vatVarPartial = None,
        paymentHistory = maybePayments,
        paymentSectionAdditionalLinks = Some(List(makePaymentLink)),
        accountBalance = accountBalance
      )
    }

    lazy val testCardWithVatVarPartial: Card = Card(
      title = "VAT",
      referenceNumber = testVrn,
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          dataSso = None
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      panelPartial = Some(panel_info(Some(false), testAppConfig, deferralPeriodOver = true)(messages).toString()),
      paymentsPartial = Some("Payments partial"),
      returnsPartial = Some("Returns partial"),
      vatVarPartial = Some("Vat Vat for Card Partial"),
      paymentHistory = Right(Nil),
      paymentSectionAdditionalLinks = Some(List(makePaymentLink)),
      accountBalance = Some(balance)
    )

    lazy val testCardNoData: Card = Card(
      title = "VAT",
      referenceNumber = testVrn,
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          dataSso = None
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      panelPartial = Some(panel_info(None, testAppConfig, deferralPeriodOver = true)(messages).toString()),
      paymentsPartial =
        Some("\n<p class=\"govuk-body\">There is no balance information to display.</p>\n"),
      returnsPartial = Some(
        "<a class=\"govuk-body govuk-link\" id=\"complete-vat-return\" href=\"http://localhost:8081/portal/vat-file/trader/"
          + testVrn +
          "/return?lang=eng\"\nrel=\"noreferrer noopener\">\n   Complete VAT Return\n</a>\n"
      ),
      vatVarPartial = None,
      paymentSectionAdditionalLinks = None,
      accountBalance = None
    )

    val makePaymentLink: Link = Link(
      id = "vat-make-payment-link",
      title = "Make a VAT payment",
      href = "http://localhost:9732/business-account/vat/make-a-payment"
    )

    lazy val service: VatCardBuilderServiceTest = new VatCardBuilderServiceTest(
      messagesApi,
      testVatPartialBuilder,
      testAppConfig,
      testVatService,
      testPaymentHistoryService,
      testLinkProviderService,
      testDate)

    val date: LocalDateTime = LocalDateTime.parse("2018-10-20T08:00:00.000")

    when(testAppConfig.getUrl(eqTo("mainPage"))).thenReturn("http://someTestUrl")
    when(
      testAppConfig.getPortalUrl(eqTo("vatFileAReturn"))(eqTo(Some(vatEnrolment)))(
        any()
      )
    ).thenReturn(
      s"http://localhost:8081/portal/vat-file/trader/$vrn/return?lang=eng"
    )
    when(testPaymentHistoryService.getPayments(eqTo(Some(vatEnrolment)))(any()))
      .thenReturn(Future.successful(Right(Nil)))
    when(
      testLinkProviderService.determinePaymentAdditionalLinks(any())(
        any(),
        any()
      )
    ).thenReturn(Some(List(makePaymentLink)))
  }

  "Calling VatCardBuilderService.buildVatCard" should {

    "return a card with No Payments information when getting VatNoData" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar
      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(None)))

      val futureResult: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      val result: Card = futureResult.futureValue

      result.title mustBe testCardNoData.title
      result.description mustBe testCardNoData.description
      result.referenceNumber mustBe testCardNoData.referenceNumber
      result.primaryLink mustBe testCardNoData.primaryLink
      result.messageReferenceKey mustBe testCardNoData.messageReferenceKey
      result.panelPartial mustBe testCardNoData.panelPartial
      result.paymentsPartial mustBe testCardNoData.paymentsPartial
      result.returnsPartial mustBe testCardNoData.returnsPartial
      result.vatVarPartial mustBe testCardNoData.vatVarPartial
      result.paymentHistory mustBe testCardNoData.paymentHistory
      result.paymentSectionAdditionalLinks mustBe testCardNoData.paymentSectionAdditionalLinks
      result mustBe testCardNoData
    }

    "return a card with a Eligible info" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithVatVar.type = VatPartialBuilderTestWithVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardWithVatVarPartial
    }

    "return a card with Payment information when getting Vat Data" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard(Some(balance))
    }

    "throw an exception when getting Vat Not Activated" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Left(VatUnactivated)))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.failed.futureValue mustBe a[Exception]
    }

    "throw an exception when getting Vat Empty" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Left(VatEmpty)))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.failed.futureValue mustBe a[Exception]

    }

    "throw an exception when getting Vat Generic Error" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Left(VatGenericError)))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.failed.futureValue mustBe a[Exception]
    }

    "return a card with payment history" in new LocalSetup {

      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(None)))

      val payments = Right(
        List(
          PaymentRecord(
            reference = "reference number",
            amountInPence = balance * balance,
            createdOn = LocalDateTime.parse("2018-10-20T08:00:00.000"),
            taxType = "tax type"
          )
        )
      )

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))
      when(testPaymentHistoryService.getPayments(Some(vatEnrolment)))
        .thenReturn(Future.successful(payments))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard(Some(BigDecimal(balance)), payments)
    }

    "return a card with a vat var partial when one is provided" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithVatVar.type = VatPartialBuilderTestWithVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))

      val result: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardWithVatVarPartial
    }
  }

  "show the correct COVID-19 panel" when {
    "the user's direct debit status is undetermined" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(None)))

      val futureResult: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      val result: Card = futureResult.futureValue

      result.panelPartial mustBe Some(panel_info(None, testAppConfig, deferralPeriodOver = true)(messages).toString())
    }

    "the user have an active the direct debit" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      override lazy val vatCalendar: Option[Calendar] = Some(
        Calendar(filingFrequency = Monthly, directDebit = ActiveDirectDebit(mock[DirectDebitActive]))
      )
      override lazy val vatData: VatData = VatData(vatAccountSummary, vatCalendar, Some(0))

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))

      val futureResult: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      val result: Card = futureResult.futureValue

      result.panelPartial mustBe Some(panel_info(Some(true), testAppConfig, deferralPeriodOver = true)(messages).toString())
    }

    "the user have an inactive the direct debit" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      override lazy val vatCalendar: Option[Calendar] = Some(
        Calendar(filingFrequency = Monthly, directDebit = InactiveDirectDebit)
      )
      override lazy val vatData: VatData = VatData(vatAccountSummary, vatCalendar, Some(0))

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))

      val futureResult: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      val result: Card = futureResult.futureValue

      result.panelPartial mustBe Some(panel_info(Some(false), testAppConfig, deferralPeriodOver = true)(messages).toString())
    }

    "the user is ineligible for direct debit" in new LocalSetup {
      val testVatPartialBuilder: VatPartialBuilderTestWithoutVatVar.type = VatPartialBuilderTestWithoutVatVar

      override lazy val vatCalendar: Option[Calendar] = Some(
        Calendar(filingFrequency = Monthly, directDebit = DirectDebitIneligible)
      )
      override lazy val vatData: VatData = VatData(vatAccountSummary, vatCalendar, Some(0))

      when(testVatService.fetchVatModel(vatEnrolment))
        .thenReturn(Future.successful(Right(Some(vatData))))

      val futureResult: Future[Card] =
        service.buildVatCard()(authenticatedRequest, hc, messages)

      val result: Card = futureResult.futureValue

      result.panelPartial mustBe Some(panel_info(Some(false), testAppConfig, deferralPeriodOver = true)(messages).toString())
    }
  }

}
