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

import config.FrontendAppConfig
import models.requests.AuthenticatedRequest
import models.{Vrn, _}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import views.ViewSpecBase
import java.time.{LocalDate, OffsetDateTime}
import scala.concurrent.Future

class VatPartialBuilderSpec
  extends ViewSpecBase
    with MockitoSugar
    with ScalaFutures with BeforeAndAfterEach {

  class testEnrolmentsStoreService(shouldShowNewPinLink: Boolean)
    extends EnrolmentsStoreService {
    def showNewPinLink(
                        enrolment: VatEnrolment,
                        currentDate: OffsetDateTime,
                        credId: String
                      )(implicit hc: HeaderCarrier): Future[Boolean] = {
      Future.successful(shouldShowNewPinLink)
    }
  }

  val mockEnrolmentsStoreService: EnrolmentsStoreService = mock[EnrolmentsStoreService]
  lazy val config: FrontendAppConfig = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEnrolmentsStoreService)
    reset(config)

    when(config.emacVatEnrolmentUrl).thenReturn( "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account")
    when(config.emacVatActivationUrl).thenReturn("/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account")
    when(config.emacVatLostPinUrl).thenReturn("/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account")
  }

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[EnrolmentsStoreService].toInstance(mockEnrolmentsStoreService),
        bind[FrontendAppConfig].toInstance(config)
      )
      .build()

  def mockShowNewPinLink(shouldShowNewPinLink: Boolean): Unit =
    when(mockEnrolmentsStoreService.showNewPinLink(any(), any(), any())(any())).thenReturn(Future.successful(shouldShowNewPinLink))

  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val vrn: Vrn = Vrn("1234567890")
  lazy val currentUrl: String = "http://someTestUrl"
  lazy val btaHomepage: String = "http://testBtaHomepage"

  trait LocalSetup {
    lazy val vatDecEnrolment: VatDecEnrolment =
      VatDecEnrolment(vrn, isActivated = true)
    lazy val vatVarEnrolment: VatEnrolment =
      VatVarEnrolment(vrn, isActivated = true)

    implicit val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] =
      requestWithEnrolment(vatDecEnrolment, vatVarEnrolment)

    def requestWithEnrolment(
                              vatDecEnrolment: VatDecEnrolment,
                              vatVarEnrolment: VatEnrolment
                            ): AuthenticatedRequest[AnyContent] = {
      AuthenticatedRequest[AnyContent](
        FakeRequest(),
        "",
        vatDecEnrolment,
        vatVarEnrolment,
        "credId"
      )
    }

    when(config.businessAccountHomeUrl).thenReturn(btaHomepage)
    when(config.getReturnUrl(eqTo(btaHomepage)))
      .thenReturn("returnUrl=" + btaHomepage)
    when(config.getReturnUrl(eqTo(fakeRequestWithEnrolments.uri))).thenReturn("returnUrl=" + currentUrl)
  }

  trait PaymentsSetup extends LocalSetup {
    lazy val accountBalance = AccountBalance(Some(BigDecimal(0.00)))
    lazy val accountSummaryData: AccountSummaryData = AccountSummaryData(
      accountBalance = Some(accountBalance),
      dateOfBalance = None,
      openPeriods = Seq.empty
    )

    lazy val activeDirectDebit: ActiveDirectDebit = ActiveDirectDebit(
      details = DirectDebitActive(
        periodEndDate = LocalDate.of(2016, 6, 30),
        periodPaymentDate = LocalDate.of(2016, 8, 15)
      )
    )

    lazy val calendarWithDirectDebit: Calendar =
      Calendar(filingFrequency = Monthly, directDebit = activeDirectDebit)

    lazy val calendarWithAnnualFiling: Calendar =
      Calendar(filingFrequency = Annually, directDebit = activeDirectDebit)

    lazy val calendarWithIneligibilityForDirectDebit: Calendar =
      Calendar(filingFrequency = Monthly, directDebit = DirectDebitIneligible)

    lazy val openPeriods: Seq[OpenPeriod] = Seq(
      OpenPeriod(LocalDate.of(2016, 6, 30)),
      OpenPeriod(LocalDate.of(2016, 5, 30))
    )

    val vatData: VatData = defaultVatData
    val vatDataWithDirectDebit: VatData =
      VatData(vatAccountSummary, Some(calendarWithDirectDebit), Some(0))

    when(config.btaManageAccount)
      .thenReturn("http://localhost:9020/business-account/manage-account")
    when(config.getHelpAndContactUrl(eqTo("howToPay")))
      .thenReturn("http://localhost:9733/business-account/help/vat/how-to-pay")
    when(config.getUrl(eqTo("makeAPayment")))
      .thenReturn("http://localhost:9732/business-account/vat/make-a-payment")
    when(
      config.getPortalUrl(eqTo("vatOnlineAccount"))(eqTo(Some(vatDecEnrolment)))(
        eqTo(fakeRequestWithEnrolments)
      )
    ).thenReturn(
      s"http://localhost:8080/portal/vat/trader/$vrn/directdebit?lang=eng"
    )
    when(
      config.getPortalUrl(eqTo("vatPaymentsAndRepayments"))(eqTo(Some(vatDecEnrolment)))(
        eqTo(fakeRequestWithEnrolments)
      )
    ).thenReturn(
      s"http://localhost:8080/portal/vat/trader/$vrn/account/overview?lang=eng"
    )
    when(
      config.getPortalUrl("vatChangeRepaymentsAccount")(Some(vatDecEnrolment))(
        fakeRequestWithEnrolments
      )
    ).thenReturn(
      s"/vat-variations/org/$vrn/introduction?lang=eng"
    )
  }

  trait ReturnsSetup extends LocalSetup {

    val testDataNoReturns =
      VatData(new AccountSummaryData(None, None, Seq()), None, Some(0))
    val testDataOneReturn =
      VatData(new AccountSummaryData(None, None, Seq()), None, Some(1))
    val testDataTwoReturns =
      VatData(new AccountSummaryData(None, None, Seq()), None, Some(2))
    val testDataNoReturnCount =
      VatData(new AccountSummaryData(None, None, Seq()), None, None)

    val testEnrolment: VatEnrolment = new VatEnrolment {
      override val isActivated: Boolean = true
      override val vrn: Vrn = Vrn("123456789")
    }

    when(
      config.getPortalUrl(eqTo("vatSubmittedReturns"))(eqTo(Some(testEnrolment)))(
        eqTo(fakeRequestWithEnrolments)
      )
    ).thenReturn(
      s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng"
    )
    when(config.getGovUrl(eqTo("vatCorrections")))
      .thenReturn("https://www.gov.uk/vat-corrections")
    when(
      config.getPortalUrl(eqTo("vatFileAReturn"))(eqTo(Some(testEnrolment)))(
        eqTo(fakeRequestWithEnrolments)
      )
    ).thenReturn(
      s"http://localhost:8080/portal/vat-file/trader/$vrn/return?lang=eng"
    )
  }

  trait VatVarSetupNoVatVal extends LocalSetup {
    override lazy val vatVarEnrolment = new VatNoEnrolment
  }

  trait VatVarSetupActiveVatVal extends LocalSetup {
    override lazy val vatVarEnrolment = VatVarEnrolment(vrn, isActivated = true)
  }

  trait VatVarSetupInactiveVatVal extends LocalSetup {
    override lazy val vatVarEnrolment =
      VatVarEnrolment(vrn, isActivated = false)
  }

  "VatPartialBuilder" should {

    "handle returns" when {
      "there are no returns to complete" in new ReturnsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildReturnsPartial(testDataNoReturns, testEnrolment)(
              fakeRequestWithEnrolments,
              messages
            )
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You have no returns to complete.")
        assertLinkById(
          doc,
          linkId = "vat-complete-return",
          expectedText = "Complete VAT Return",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/return?lang=eng",

          expectedOpensInNewTab = true
        )
        assertLinkById(
          doc,
          linkId = "view-submitted-returns",
          expectedText = "View submitted returns (opens in new tab)",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng",

          expectedOpensInNewTab = true
        )
      }

      "there is one return to complete" in new ReturnsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildReturnsPartial(testDataOneReturn, testEnrolment)(
              fakeRequestWithEnrolments,
              messages
            )
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("A VAT Return is ready to complete")
        assertLinkById(
          doc,
          linkId = "vat-complete-return",
          expectedText = "Complete VAT Return",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/return?lang=eng",

          expectedOpensInNewTab = true
        )
        assertLinkById(
          doc,
          linkId = "view-submitted-returns",
          expectedText = "View submitted returns (opens in new tab)",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng",

          expectedOpensInNewTab = true
        )
      }

      "there are multiple returns to complete" in new ReturnsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildReturnsPartial(testDataTwoReturns, testEnrolment)(
              fakeRequestWithEnrolments,
              messages
            )
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("2 VAT Returns are ready to complete")
        assertLinkById(
          doc,
          linkId = "vat-complete-returns",
          expectedText = "Complete VAT Returns",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/return?lang=eng",

          expectedOpensInNewTab = true
        )
        assertLinkById(
          doc,
          linkId = "view-submitted-returns",
          expectedText = "View submitted returns (opens in new tab)",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng",

          expectedOpensInNewTab = true
        )
      }

      "the return count is not available" in new ReturnsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildReturnsPartial(testDataNoReturnCount, testEnrolment)(
              fakeRequestWithEnrolments,
              messages
            )
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() mustNot include("ready to complete")
        assertLinkById(
          doc,
          linkId = "complete-vat-return",
          expectedText = "Complete VAT Return",
          expectedUrl =
            s"http://localhost:8080/portal/vat-file/trader/$vrn/return?lang=eng",

          expectedOpensInNewTab = true
        )
      }
    }

    "handle payments" when {
      "the user is in credit with nothing to pay" in new PaymentsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(-12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods)
        )

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You are £12.34 in credit.")
        doc.text() must include(
          "View and manage your repayment details"
        )
        assertLinkById(
          doc,
          linkId = "vat-repayments-account",
          expectedText = "View and manage your repayment details",
          expectedUrl =
            s"/vat-repayment-tracker/show-vrt",
          expectedIsExternal = false,
          expectedOpensInNewTab = false
        )
      }

      "the user is in debit and has no Direct Debit set up" in new PaymentsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods)
        )

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text().contains("You owe £12.34") mustBe true
      }

      "the user is in debit and has a Direct Debit set up" in new PaymentsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods),
          calendar = Some(calendarWithDirectDebit)
        )
        val view: String =
          inject[VatPartialBuilderImpl]
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You owe £12.34")
        doc.text() must include(
          "You have a VAT Direct Debit. If you complete your return on time, we will take payment for the period ending 30 June 2016 on 15 August 2016."
        )
      }

      "the user is in debit and files annually (should not see DD)" in new PaymentsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods),
          calendar = Some(calendarWithAnnualFiling)
        )

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You owe £12.34")
        doc.text() mustNot include("Direct Debit")
      }

      "the user is in debit but ineligible for Direct Debit (should not see DD)" in new PaymentsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods),
          calendar = Some(calendarWithIneligibilityForDirectDebit)
        )

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You owe £12.34")
        doc.text() mustNot include("Direct Debit")
      }

      "the user has no tax to pay" in new PaymentsSetup {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods)
        )

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text().contains("You have no tax to pay.") mustBe true

      }
    }

    "handle Vat Var content for Cards" when {

      "no vat var enrolment exists" in new VatVarSetupNoVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(true)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc
          .getElementById("change-vat-details-header")
          .text() mustBe "Change VAT details online"
        assertLinkById(
          doc,
          linkId = "change-vat-details",
          expectedText = "Request access to change your VAT details online",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account"
        )
      }

      "an activated vat var enrolment exists" in new VatVarSetupActiveVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: Future[Option[Html]] =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(true)(fakeRequestWithEnrolments, messages, hc)
        view.futureValue mustBe None
      }

      "an unactivated vat var enrolment exists and it is within 7 days of application" in new VatVarSetupInactiveVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(true)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc
          .getElementById("change-vat-details-header")
          .text() mustBe "Change VAT details online"
        doc.text() must include(
          "We posted an activation code to you. Delivery takes up to 7 days."
        )
        assertLinkById(
          doc,
          linkId = "activate-vat-var",
          expectedText =
            "Use the activation code so you can change your VAT details online",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://testBtaHomepage"
        )
        doc.text() must include(
          "It can take up to 72 hours to display your details."
        )
      }

      "an unactivated vat var enrolment exists and it is more than 7 days since application" in new VatVarSetupInactiveVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = true)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(true)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc
          .getElementById("change-vat-details-header")
          .text() mustBe "Change VAT details online"
        doc.text() must include(
          "Use the activation code we posted to you so you can"
        )
        assertLinkById(
          doc,
          linkId = "activate-vat-var",
          expectedText = "change your VAT details online",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://testBtaHomepage"
        )
        doc.text() must include(
          "It can take up to 72 hours to display your details."
        )
        doc.text() must include("You can")
        assertLinkById(
          doc,
          linkId = "vat-var-new-code",
          expectedText = "request a new activation code",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account"
        )
      }
    }

    "handle Vat Var content for the subpage" when {

      "no vat var enrolment exists" in new VatVarSetupNoVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(false)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc.text() must include(
          "! Warning Set up your VAT so you can change your details online (opens in new tab)."
        )
        assertLinkById(
          doc,
          linkId = "vat-activate-or-enrol-details-summary",
          expectedText =
            "Set up your VAT so you can change your details online (opens in new tab)",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account",

          expectedOpensInNewTab = true
        )
      }

      "an activated vat var enrolment exists" in new VatVarSetupActiveVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: Future[Option[Html]] =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(false)(fakeRequestWithEnrolments, messages, hc)

        view.futureValue mustBe None
      }

      "an unactivated vat var enrolment exists and it is within 7 days of application" in new VatVarSetupInactiveVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = false)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(false)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc
          .getElementById("change-vat-details-header")
          .text() mustBe "Change VAT details online"
        doc.text() must include(
          "We posted an activation code to you. Delivery takes up to 7 days."
        )
        assertLinkById(
          doc,
          linkId = "activate-vat-var",
          expectedText =
            "Use the activation code so you can change your VAT details online",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://someTestUrl"
        )
        doc.text() must include(
          "It can take up to 72 hours to display your details."
        )
      }

      "an unactivated vat var enrolment exists and it is more than 7 days since application" in new VatVarSetupInactiveVatVal {
        mockShowNewPinLink(shouldShowNewPinLink = true)

        val view: String =
          inject[VatPartialBuilderImpl]
            .buildVatVarPartial(false)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc
          .getElementById("change-vat-details-header")
          .text() mustBe "Change VAT details online"
        doc.text() must include(
          "Use the activation code we posted to you so you can"
        )
        assertLinkById(
          doc,
          linkId = "activate-vat-var",
          expectedText = "change your VAT details online",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://someTestUrl"
        )
        doc.text() must include(
          "It can take up to 72 hours to display your details."
        )
        doc.text() must include("You can")
        assertLinkById(
          doc,
          linkId = "vat-var-new-code",
          expectedText = "request a new activation code",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account"
        )
      }
    }

  }

}
