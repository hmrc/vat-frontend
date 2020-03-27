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

import config.FrontendAppConfig
import connectors.models._
import models._
import models.requests.AuthenticatedRequest
import org.joda.time.{DateTime, LocalDate}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import views.ViewSpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatPartialBuilderSpec
    extends ViewSpecBase
    with OneAppPerSuite
    with MockitoSugar
    with ScalaFutures
    with MustMatchers {

  class testEnrolmentsStoreService(shouldShowNewPinLink: Boolean)
    extends EnrolmentsStoreService {
    def showNewPinLink(
                        enrolment: VatEnrolment,
                        currentDate: DateTime,
                        credId: String
                      )(implicit hc: HeaderCarrier): Future[Boolean] = {
      Future.successful(shouldShowNewPinLink)
    }
  }

  trait LocalSetup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(FakeRequest())
    lazy val vrn: Vrn = Vrn("1234567890")
    lazy val config: FrontendAppConfig = mock[FrontendAppConfig]
    lazy val currentUrl: String = "http://someTestUrl"
    lazy val btaHomepage: String = "http://testBtaHomepage"

    lazy val vatDecEnrolment: VatDecEnrolment =
      VatDecEnrolment(vrn, isActivated = true)
    lazy val vatVarEnrolment: VatEnrolment =
      VatVarEnrolment(vrn, isActivated = true)

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
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
    when(config.getReturnUrl(btaHomepage))
      .thenReturn("returnUrl=" + btaHomepage)
    when(config.getReturnUrl(request.uri)).thenReturn("returnUrl=" + currentUrl)
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
        periodEndDate = new LocalDate(2016, 6, 30),
        periodPaymentDate = new LocalDate(2016, 8, 15)
      )
    )

    lazy val calendarWithDirectDebit: Calendar =
      Calendar(filingFrequency = Monthly, directDebit = activeDirectDebit)

    lazy val calendarWithAnnualFiling: Calendar =
      Calendar(filingFrequency = Annually, directDebit = activeDirectDebit)

    lazy val calendarWithIneligibilityForDirectDebit: Calendar =
      Calendar(filingFrequency = Monthly, directDebit = DirectDebitIneligible)

    lazy val openPeriods: Seq[OpenPeriod] = Seq(
      OpenPeriod(new LocalDate(2016, 6, 30)),
      OpenPeriod(new LocalDate(2016, 5, 30))
    )

    val vatData: VatData = defaultVatData
    val vatDataWithDirectDebit: VatData =
      VatData(vatAccountSummary, Some(calendarWithDirectDebit), Some(0))

    when(config.btaManageAccount)
      .thenReturn("http://localhost:9020/business-account/manage-account")
    when(config.getHelpAndContactUrl("howToPay"))
      .thenReturn("http://localhost:9733/business-account/help/vat/how-to-pay")
    when(config.getUrl("makeAPayment"))
      .thenReturn("http://localhost:9732/business-account/vat/make-a-payment")
    when(
      config.getPortalUrl("vatOnlineAccount")(Some(vatDecEnrolment))(
        fakeRequestWithEnrolments
      )
    ).thenReturn(
      s"http://localhost:8080/portal/vat/trader/$vrn/directdebit?lang=eng"
    )
    when(
      config.getPortalUrl("vatPaymentsAndRepayments")(Some(vatDecEnrolment))(
        fakeRequestWithEnrolments
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
      config.getPortalUrl("vatSubmittedReturns")(Some(testEnrolment))(
        fakeRequestWithEnrolments
      )
    ).thenReturn(
      s"http://localhost:8080/portal/vat-file/trader/$vrn/periods?lang=eng"
    )
    when(config.getGovUrl("vatCorrections"))
      .thenReturn("https://www.gov.uk/vat-corrections")
    when(
      config.getPortalUrl("vatFileAReturn")(Some(testEnrolment))(
        fakeRequestWithEnrolments
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
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
          expectedGAEvent =
            "link - click:VAT cards:Complete VAT Return - No returns",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "there is one return to complete" in new ReturnsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
          expectedGAEvent = "link - click:VAT cards:Complete VAT Return",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "there are multiple returns to complete" in new ReturnsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
          expectedGAEvent = "link - click:VAT cards:Complete VAT Returns",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "the return count is not available" in new ReturnsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
          expectedGAEvent = "link - click:VAT cards:Complete VAT Return",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }
    }

    "handle payments" when {
      "the user is in credit with nothing to pay" in new PaymentsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(-12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods)
        )

        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You are £12.34 in credit.")
        doc.text() must include(
          "If you have set up a repayments bank account, we will transfer you this money. If not, we will send you a cheque in the post. Repayments are usually made within 30 days of HMRC getting your VAT return."
        )
        assertLinkById(
          doc,
          linkId = "vat-repayments-account",
          expectedText = "repayments bank account",
          expectedUrl =
            s"/vat-variations/org/$vrn/introduction?lang=eng",
          expectedGAEvent =
            "link - click:VATaccountSummary:repayments bank account",
          expectedIsExternal = false,
          expectedOpensInNewTab = false
        )
      }

      "the user is in debit and has no Direct Debit set up" in new PaymentsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods)
        )

        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text().contains("You owe £12.34") mustBe true
      }

      "the user is in debit and has a Direct Debit set up" in new PaymentsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods),
          calendar = Some(calendarWithDirectDebit)
        )
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You owe £12.34")
        doc.text() must include(
          "You have a VAT Direct Debit. If you complete your return on time, we will take payment for the period ending 30 June 2016 on 15 August 2016."
        )
      }

      "the user is in debit and files annually (should not see DD)" in new PaymentsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods),
          calendar = Some(calendarWithAnnualFiling)
        )

        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You owe £12.34")
        doc.text() mustNot include("Direct Debit")
      }

      "the user is in debit but ineligible for Direct Debit (should not see DD)" in new PaymentsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        override lazy val accountBalance =
          AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods),
          calendar = Some(calendarWithIneligibilityForDirectDebit)
        )

        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text() must include("You owe £12.34")
        doc.text() mustNot include("Direct Debit")
      }

      "the user has no tax to pay" in new PaymentsSetup {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        override val vatData = defaultVatData.copy(
          accountSummary = accountSummaryData.copy(openPeriods = openPeriods)
        )

        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages)
            .body
        val doc: Document = Jsoup.parse(view)

        doc.text().contains("You have no tax to pay.") mustBe true

      }
    }

    "handle Vat Var content for Cards" when {

      "no vat var enrolment exists" in new VatVarSetupNoVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account",
          expectedGAEvent =
            "link - click:VAT cards:Request access to change your VAT details online"
        )
      }

      "an activated vat var enrolment exists" in new VatVarSetupActiveVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: Future[Option[Html]] =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildVatVarPartial(true)(fakeRequestWithEnrolments, messages, hc)
        view.futureValue mustBe None
      }

      "an unactivated vat var enrolment exists and it is within 7 days of application" in new VatVarSetupInactiveVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://testBtaHomepage",
          expectedGAEvent =
            "link - click:VAT cards:change your VAT details online"
        )
        doc.text() must include(
          "It can take up to 72 hours to display your details."
        )
      }

      "an unactivated vat var enrolment exists and it is more than 7 days since application" in new VatVarSetupInactiveVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(true)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://testBtaHomepage",
          expectedGAEvent =
            "link - click:VAT cards:change your VAT details online"
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account",
          expectedGAEvent =
            "link - click:VAT cards:Request a new vat var activation code"
        )
      }
    }

    "handle Vat Var content for the subpage" when {

      "no vat var enrolment exists" in new VatVarSetupNoVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildVatVarPartial(false)(fakeRequestWithEnrolments, messages, hc)
            .futureValue
            .get
            .body

        val doc: Document = Jsoup.parse(view)

        doc.text() must include(
          "Important information Set up your VAT so you can change your details online (opens in a new window or tab)."
        )
        assertLinkById(
          doc,
          linkId = "vat-activate-or-enrol-details-summary",
          expectedText =
            "Set up your VAT so you can change your details online (opens in a new window or tab)",
          expectedUrl =
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account",
          expectedGAEvent = "link - click:VATVar:set up now",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "an activated vat var enrolment exists" in new VatVarSetupActiveVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: Future[Option[Html]] =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
            .buildVatVarPartial(false)(fakeRequestWithEnrolments, messages, hc)

        view.futureValue mustBe None
      }

      "an unactivated vat var enrolment exists and it is within 7 days of application" in new VatVarSetupInactiveVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(false)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://someTestUrl",
          expectedGAEvent = "link - click:VATVar:Enter pin"
        )
        doc.text() must include(
          "It can take up to 72 hours to display your details."
        )
      }

      "an unactivated vat var enrolment exists and it is more than 7 days since application" in new VatVarSetupInactiveVatVal {
        val enrolmentStore: testEnrolmentsStoreService =
          new testEnrolmentsStoreService(true)
        val view: String =
          new VatPartialBuilderImpl(enrolmentStore, emacUrlBuilder, config)
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http://someTestUrl",
          expectedGAEvent = "link - click:VATVar:Enter pin"
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
            "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account",
          expectedGAEvent = "link - click:VATVar:Lost pin"
        )
      }
    }

  }

}
