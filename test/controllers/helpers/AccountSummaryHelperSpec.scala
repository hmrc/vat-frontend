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

package controllers.helpers

import connectors.models._
import models._
import models.payment.PaymentRecord
import models.requests.AuthenticatedRequest
import org.joda.time.{DateTime, LocalDate}
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase

import scala.collection.JavaConverters._
import scala.concurrent.Future

class AccountSummaryHelperSpec
    extends ViewSpecBase
    with MockitoSugar
    with ScalaFutures {

  val accountSummary: AccountSummaryData = AccountSummaryData(
    Some(AccountBalance(Some(BigDecimal(0.00)))),
    None,
    Seq.empty
  )
  val mockVatService: VatService = mock[VatService]

  val testCurrentUrl = "testUrl"

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

  def requestWithURI(
    vatDecEnrolment: VatDecEnrolment,
    vatVarEnrolment: VatEnrolment
  ): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](
      FakeRequest()
        .copyFakeRequest(uri = "http://localhost:9732/business-account/vat"),
      "",
      vatDecEnrolment,
      vatVarEnrolment,
      "credId"
    )
  }

  val vatDecEnrolment = VatDecEnrolment(Vrn("vrn"), isActivated = true)
  val vatVarEnrolment = VatVarEnrolment(Vrn("vrn"), isActivated = true)

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] =
    requestWithEnrolment(vatDecEnrolment, vatVarEnrolment)

  def accountSummaryHelper() =
    new AccountSummaryHelper(
      frontendAppConfig,
      mockVatService,
      emacUrlBuilder,
      messagesApi
    )

  "getAccountSummaryView" when {
    "there is an empty account summary" should {
      "show a complete return button, make a payment button and correct message" in {

        val result =
          accountSummaryHelper().getAccountSummaryView(Right(None), Right(Nil))(
            fakeRequestWithEnrolments
          )
        val doc = asDocument(result)
        doc
          .getElementById("vat-file-return-link")
          .text mustBe "Complete VAT return"
        doc
          .getElementById("vat-make-payment-link")
          .text mustBe "Make a VAT payment"
        doc.text() must include("No balance information to display")

        assertLinkById(
          doc,
          "vat-make-payment-link",
          "Make a VAT payment",
          "http://localhost:9732/business-account/vat/make-a-payment",
          "link - click:VATaccountSummary:Make a VAT payment"
        )
      }
    }
  }

  "there is an account summary to render with open periods" should {
    "show a complete return button and correct message for each open period" in {

      val testOpenPeriods: Seq[OpenPeriod] = Seq(
        OpenPeriod(new LocalDate(2016, 6, 30)),
        OpenPeriod(new LocalDate(2016, 5, 30))
      )

      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(openPeriods = testOpenPeriods)
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)

      val doc = asDocument(result)
      val periodList = doc.getElementsByClass("flag--soon").asScala.toList

      periodList.length mustBe 2
      doc
        .getElementById("vat-file-return-link")
        .text mustBe "Complete VAT return"
      doc.text() must include(s"Return for period ending 30 June 2016")
      doc.text() must include(s"Return for period ending 30 May 2016")

    }
  }

  "there is an account summary to render with no open periods and account balance is zero" should {
    "show correct message with view statement link" in {
      val vatData = defaultVatData.copy(
        accountSummary =
          accountSummary.copy(accountBalance = Some(AccountBalance(Some(0))))
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("You have nothing to pay")
      doc.text() must not include "Return for period ending"
      assertLinkById(
        doc,
        "vat-see-breakdown-link",
        "View your VAT statement (opens in a new window or tab)",
        "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
        "link - click:VATaccountSummary:how we worked this out OR view statement",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )
    }
  }

  "there is an account summary to render and account is in credit" should {
    val creditBalance = Some(AccountBalance(Some(BigDecimal(-500.00))))

    "show correct message with see breakdown link" in {
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance)
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("You are £500.00 in credit")
      assertLinkById(
        doc,
        "vat-see-breakdown-link",
        "How we worked out your balance (opens in a new window or tab)",
        "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
        "link - click:VATaccountSummary:how we worked this out OR view statement",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )
    }

    "there is an account summary to render and account is in credit with pennies in the credit value" should {
      val creditBalance = Some(AccountBalance(Some(BigDecimal(-500.12))))

      "show correct message with see breakdown link" in {
        val vatData = defaultVatData.copy(
          accountSummary = accountSummary.copy(accountBalance = creditBalance)
        )
        val result = accountSummaryHelper().getAccountSummaryView(
          Right(Some(vatData)),
          Right(Nil)
        )(fakeRequestWithEnrolments)
        val doc = asDocument(result)
        doc.text() must include("You are £500.12 in credit")
        assertLinkById(
          doc,
          "vat-see-breakdown-link",
          "How we worked out your balance (opens in a new window or tab)",
          "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
          "link - click:VATaccountSummary:how we worked this out OR view statement",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }
    }

    "there is an account summary to render and account is in credit with pennies in the credit value and the dateOfBalance is blank" should {
      val creditBalance = Some(AccountBalance(Some(BigDecimal(-500.12))))

      "show correct message with see breakdown link" in {
        val vatData = defaultVatData.copy(
          accountSummary = accountSummary
            .copy(accountBalance = creditBalance, dateOfBalance = None)
        )
        val result = accountSummaryHelper().getAccountSummaryView(
          Right(Some(vatData)),
          Right(Nil)
        )(fakeRequestWithEnrolments)
        val doc = asDocument(result)
        doc.text() must include("You are £500.12 in credit")
        assertLinkById(
          doc,
          "vat-see-breakdown-link",
          "How we worked out your balance (opens in a new window or tab)",
          "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
          "link - click:VATaccountSummary:how we worked this out OR view statement",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }
    }

    "there is an account summary to render and account is in credit by more than Int.max" should {
      val creditBalance =
        Some(AccountBalance(Some(BigDecimal(-1234567890123.12))))

      "show correct message with see breakdown link" in {
        val vatData = defaultVatData.copy(
          accountSummary = accountSummary.copy(accountBalance = creditBalance)
        )
        val result = accountSummaryHelper().getAccountSummaryView(
          Right(Some(vatData)),
          Right(Nil)
        )(fakeRequestWithEnrolments)
        val doc = asDocument(result)
        doc.text() must include("You are £1,234,567,890,123.12 in credit")
        assertLinkById(
          doc,
          "vat-see-breakdown-link",
          "How we worked out your balance (opens in a new window or tab)",
          "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
          "link - click:VATaccountSummary:how we worked this out OR view statement",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }
    }

    "have expandable content about repayments when filing frequency is not annual" in {

      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance)
      )

      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      val repaymentContent = doc.getElementsByClass("panel-indent").first.text

      doc.getElementById("vat-when-repaid").text mustBe "When you'll be repaid"

      repaymentContent must include(
        "We'll transfer this amount to your repayments bank account if you've set one up." +
          " We'll post you a payable order (like a cheque) otherwise."
      )
      repaymentContent must include(
        "We normally send payment within 10 days unless we need to make checks," +
          " for example if you're reclaiming more VAT than usual."
      )
      repaymentContent must include(
        "If you have been in credit for more than 21 days, you can "
      )

      assertLinkById(
        doc,
        "vat-repayments-account",
        "repayments bank account",
        "http://localhost:8080/portal/vat-variations/org/vrn/introduction?lang=eng",
        "link - click:VATaccountSummary:repayments bank account"
      )
      assertLinkById(
        doc,
        "vat-more-than-21-days",
        "contact VAT helpline (opens in a new window or tab)",
        "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/vat-enquiries",
        "link - click:VATaccountSummary:contact VAT helpline",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )
    }

    "not have expandable content about repayments when filing frequency is annual" in {

      val annualCalendar = calendar.copy(filingFrequency = Annually)
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance),
        calendar = Some(annualCalendar)
      )

      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must not include "When you'll be repaid"
      doc.text() must not include ("We'll transfer this amount to your repayments bank account if you've set one up." +
        " We'll post you a payable order (like a cheque) otherwise.")
      doc.text() must not include ("We normally send payment within 10 days unless we need to make checks," +
        " for example if you're reclaiming more VAT than usual.")
      doc.text() must not include "Don't get in touch unless you've been in credit for more than 21 days."
    }

    "not have 'Set up a Direct Debit' link when direct debit is not eligible" in {

      val directDebitEligible =
        Some(calendar.copy(directDebit = DirectDebitIneligible))
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance),
        calendar = directDebitEligible
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)

      doc.text() must not include "Set up a Direct Debit"
    }

    "have 'Set up a Direct Debit' link when direct debit is eligible but not active" in {

      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance)
      )

      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)

      assertLinkById(
        doc,
        "vat-direct-debit-setup-link",
        "Set up a Direct Debit (opens in a new window or tab)",
        "http://localhost:8080/portal/vat/trader/vrn/directdebit?lang=eng",
        "link - click:VATaccountSummary:Set up a Direct Debit",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )

      doc.text() must not include "You've set up a Direct Debit to pay VAT"
      doc.text() must not include "We'll take payment for the period ending"
      doc.text() must not include "as long as you file your return on time."

    }

    "not have Direct Debit information when direct debit is eligible and inactive, but the user files annually" in {
      val directDebitAnnualFiling =
        Some(calendar.copy(filingFrequency = Annually))
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance),
        calendar = directDebitAnnualFiling
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)

      val doc = asDocument(result)
      doc.text() must not include "Set up a Direct Debit"
      doc.text() must not include "We'll take payment for the period"
    }

    "not have Direct Debit information when direct debit is eligible and active, but the user files annually" in {

      val directDebitAnnualFiling = Some(
        calendar.copy(
          directDebit = ActiveDirectDebit(
            details = DirectDebitActive(
              new LocalDate(2016, 6, 30),
              new LocalDate(2016, 8, 15)
            )
          ),
          filingFrequency = Annually
        )
      )
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance),
        calendar = directDebitAnnualFiling
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must not include "Set up a Direct Debit"
      doc.text() must not include "We'll take payment for the period"
    }

    "have expandable content about direct debits when direct debit is eligible and active" in {

      val dDActive = Some(
        calendar.copy(
          directDebit = ActiveDirectDebit(
            details = DirectDebitActive(
              new LocalDate(2016, 6, 30),
              new LocalDate(2016, 8, 15)
            )
          )
        )
      )

      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = creditBalance),
        calendar = dDActive
      )

      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)

      doc
        .getElementById("vat-direct-debit-see-detail")
        .text mustBe "You've set up a Direct Debit to pay VAT"

      doc.text() must include(
        "We'll take payment for the period ending 30 June 2016" +
          " on 15 August 2016 as long as you file your return on time"
      )
    }
  }

  "there is an account summary to render and account balance is greater than zero" should {
    val dueBalance = Some(AccountBalance(Some(BigDecimal(50.00))))
    val vatData = defaultVatData.copy(
      accountSummary = accountSummary.copy(accountBalance = dueBalance)
    )

    "show correct message with see breakdown link" in {
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("You owe £50.00")
      doc.text() must include(
        "You can no longer use a personal credit card. If you pay with a credit card, it must be linked to a business bank account."
      )
      assertLinkById(
        doc,
        "vat-see-breakdown-link",
        "How we worked out your balance (opens in a new window or tab)",
        "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
        "link - click:VATaccountSummary:how we worked this out OR view statement",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )
    }

    "not show personal credit card message when Boolean is false" in {
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil),
        showCreditCardMessage = false
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must not include "You can no longer use a personal credit card. If you pay with a credit card, it must be linked to a business bank account."
    }
  }

  "there is an account summary to render and account balance is greater than zero and the sum owed is known to the penny" should {
    "show correct message with see breakdown link" in {
      val dueBalance = Some(AccountBalance(Some(BigDecimal(12.34))))
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = dueBalance)
      )

      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("You owe £12.34")
      assertLinkById(
        doc,
        "vat-see-breakdown-link",
        "How we worked out your balance (opens in a new window or tab)",
        "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
        "link - click:VATaccountSummary:how we worked this out OR view statement",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )
    }
  }

  "there is an account summary to render and account balance is greater than zero by more than Int.Max" should {
    "show correct message with see breakdown link" in {
      val dueBalance = Some(AccountBalance(Some(BigDecimal(12345678901.89))))
      val vatData = defaultVatData.copy(
        accountSummary = accountSummary.copy(accountBalance = dueBalance)
      )

      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("You owe £12,345,678,901.89")
      assertLinkById(
        doc,
        "vat-see-breakdown-link",
        "How we worked out your balance (opens in a new window or tab)",
        "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
        "link - click:VATaccountSummary:how we worked this out OR view statement",
        expectedIsExternal = true,
        expectedOpensInNewTab = true
      )
    }
  }

  "there is an error retrieving the data" should {
    "return the generic error message" in {
      reset(mockVatService)
      when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Left(VatGenericError)))
      val view = accountSummaryHelper().getAccountSummaryView(
        Left(VatGenericError),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      view.toString must include(
        "We can’t display your VAT information at the moment."
      )
    }
  }

  "payment history" should {
    "not display payments title if user has no payments to display" in {
      val vatData = defaultVatData
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(Nil)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must not include "Your card payments in the last 7 days"
    }

    "display when a user has a single payment history record" in {

      val history = List(
        PaymentRecord(
          reference = "TEST56",
          amountInPence = 100,
          createdOn = new DateTime("2018-10-21T08:00:00.000"),
          taxType = "tax type"
        )
      )
      val vatData = defaultVatData.copy(
        accountSummary =
          accountSummary.copy(accountBalance = Some(AccountBalance(Some(0))))
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(history)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("Your card payments in the last 7 days")
      doc.text() must include("You paid £1 on 21 October 2018")
      doc.text() must include("Your payment reference number is TEST56.")
      doc.text() must include(
        "It will take up to 7 days to update your balance after each payment."
      )
    }

    "display when a user has multiple payment history records" in {

      val history = List(
        PaymentRecord(
          reference = "TEST56",
          amountInPence = 100,
          createdOn = new DateTime("2018-10-21T08:00:00.000"),
          taxType = "tax type"
        ),
        PaymentRecord(
          reference = "TEST56",
          amountInPence = 200,
          createdOn = new DateTime("2018-10-22T08:00:00.000"),
          taxType = "tax type"
        )
      )

      val vatData = defaultVatData.copy(
        accountSummary =
          accountSummary.copy(accountBalance = Some(AccountBalance(Some(0))))
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(history)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("Your card payments in the last 7 days")
      doc.text() must include("You paid £1 on 21 October 2018")
      doc.text() must include("You paid £2 on 22 October 2018")
      doc.text() must not include "Your payment reference number is TEST56."
      doc.text() must include(
        "It will take up to 7 days to update your balance after each payment."
      )
    }

    "correctly format amount to £20.10" in {
      val history = List(
        PaymentRecord(
          reference = "TEST58",
          amountInPence = 2010,
          createdOn = new DateTime("2018-10-21T08:00:00.000"),
          taxType = "tax type"
        )
      )
      val vatData = defaultVatData.copy(
        accountSummary =
          accountSummary.copy(accountBalance = Some(AccountBalance(Some(0))))
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(history)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("Your card payments in the last 7 days")
      doc.text() must include("You paid £20.10 on 21 October 2018")
      doc.text() must include("Your payment reference number is TEST58.")
      doc.text() must include(
        "It will take up to 7 days to update your balance after each payment."
      )
    }

    "correctly format amount to £2,000.76" in {
      val history = List(
        PaymentRecord(
          reference = "TEST58",
          amountInPence = 200076,
          createdOn = new DateTime("2018-10-21T08:00:00.000"),
          taxType = "tax type"
        )
      )
      val vatData = defaultVatData.copy(
        accountSummary =
          accountSummary.copy(accountBalance = Some(AccountBalance(Some(0))))
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(history)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("Your card payments in the last 7 days")
      doc.text() must include("You paid £2,000.76 on 21 October 2018")
      doc.text() must include("Your payment reference number is TEST58.")
      doc.text() must include(
        "It will take up to 7 days to update your balance after each payment."
      )
    }

    "handle displaying 1000000000000" in {
      val history = List(
        PaymentRecord(
          reference = "TEST58",
          amountInPence = 1000000000000L,
          createdOn = new DateTime("2018-10-21T08:00:00.000"),
          taxType = "tax type"
        )
      )
      val vatData = defaultVatData.copy(
        accountSummary =
          accountSummary.copy(accountBalance = Some(AccountBalance(Some(0))))
      )
      val result = accountSummaryHelper().getAccountSummaryView(
        Right(Some(vatData)),
        Right(history)
      )(fakeRequestWithEnrolments)
      val doc = asDocument(result)
      doc.text() must include("Your card payments in the last 7 days")
      doc.text() must include("You paid £10,000,000,000 on 21 October 2018")
      doc.text() must include("Your payment reference number is TEST58.")
      doc.text() must include(
        "It will take up to 7 days to update your balance after each payment."
      )

    }

  }

}
