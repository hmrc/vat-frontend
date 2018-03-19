/*
 * Copyright 2018 HM Revenue & Customs
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
import models._
import models.requests.AuthenticatedRequest
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase

import scala.collection.JavaConverters._
import scala.concurrent.Future

class AccountSummaryHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val accountSummary: AccountSummaryData = AccountSummaryData(Some(AccountBalance(Some(BigDecimal(0.00)))), None, Seq.empty)
  val calendar: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(true, None), None, Seq()))
  val mockVatService: VatService = mock[VatService]

  def requestWithEnrolment(vatDecEnrolment: VatDecEnrolment, vatVarEnrolment: VatEnrolment): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vatDecEnrolment, vatVarEnrolment)
  }

  val vatDecEnrolment= VatDecEnrolment(Vrn("vrn"), isActivated = true)
  val vatVarEnrolment = VatVarEnrolment(Vrn("vrn"), isActivated = true)

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(vatDecEnrolment, vatVarEnrolment)

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockVatService, messagesApi)

  "getAccountSummaryView" when {
    "there is an empty account summary" should {
      "show a complete return button, make a payment button and correct message" in {
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(VatNoData))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          doc.getElementById("vat-file-return-link").text mustBe "Complete your VAT return (opens in HMRC online)"
          doc.getElementById("vat-make-payment-link").text mustBe "Make a VAT payment"
          doc.text() must include("No balance information to display")
        }
      }
    }

    "there is an account summary to render with open periods" should {
      "show a complete return button and correct message for each open period" in {

        val testOpenPeriods: Seq[OpenPeriod] = Seq(OpenPeriod(new LocalDate(2016, 6, 30)), OpenPeriod(new LocalDate(2016, 5, 30)))

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(openPeriods = testOpenPeriods), calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          val periodList = doc.getElementsByClass("flag--soon").asScala.toList

          periodList.length mustBe 2
          doc.getElementById("vat-file-return-link").text mustBe "Complete your VAT return (opens in HMRC online)"
          doc.text() must include(s"Return for period ending 30 June 2016")
          doc.text() must include(s"Return for period ending 30 May 2016")
        }
      }
    }

    "there is an account summary to render with no open periods and account balance is zero" should {
      "show correct message with view statement link" in {
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary, calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          doc.text() must include("You have nothing to pay")
          doc.text() must not include "Return for period ending"
          assertLinkById(doc,
            "vat-see-breakdown-link",
            "view statement",
            "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
            "HomepageVAT:click:SeeBreakdown")
        }
      }
    }

    "there is an account summary to render and account is in credit" should {
      val creditBalance = Some(AccountBalance(Some(BigDecimal(-500.00))))
      "show correct message with see breakdown link" in {

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          doc.text() must include("You are £500.00 in credit")
          assertLinkById(doc,
            "vat-see-breakdown-link",
            "see breakdown",
            "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
            "HomepageVAT:click:SeeBreakdown")
        }
      }
      "have expandable content about repayments when filing frequency is not annual" in {

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          val repaymentContent = doc.getElementsByClass("panel-indent").first.text

          doc.getElementById("vat-when-repaid").text mustBe "When you'll be repaid"

          repaymentContent must include("We'll transfer this amount to your repayments bank account if you've set one up." +
            " We'll post you a payable order (like a cheque) otherwise.")
          repaymentContent must include("We normally send payment within 10 days unless we need to make checks," +
            " for example if you're reclaiming more VAT than usual.")
          repaymentContent must include("Don't get in touch unless you've been in credit for more than 21 days.")

          assertLinkById(doc,
            "vat-repayments-account",
            "repayments bank account",
            "http://localhost:9020/business-account/manage-account#bank",
            "VAT:click:RepaymentsBankAccount")
          assertLinkById(doc,
            "vat-more-than-21-days",
            "more than 21 days",
            "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/vat-enquiries",
            "VAT:click:MoreThan21Days")
        }
      }
      "not have expandable content about repayments when filing frequency is annual" in {

        val annualCalendar = Some(calendar.get.copy(staggerCode = Some("0004")))

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), annualCalendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)

          doc.text() must not include "When you'll be repaid"
          doc.text() must not include ("We'll transfer this amount to your repayments bank account if you've set one up." +
            " We'll post you a payable order (like a cheque) otherwise.")
          doc.text() must not include ("We normally send payment within 10 days unless we need to make checks," +
            " for example if you're reclaiming more VAT than usual.")
          doc.text() must not include "Don't get in touch unless you've been in credit for more than 21 days."

        }
      }
      "not have 'Set up a Direct Debit' link when direct debit is not eligible" in {

        val directDebitEligible = Some(calendar.get.copy(directDebit = DirectDebit(ddiEligibilityInd = false, active = None)))

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), directDebitEligible)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)

          doc.text() must not include "Set up a Direct Debit"
        }
      }
      "have 'Set up a Direct Debit' link when direct debit is eligible but not active" in {

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)

          assertLinkById(doc,
            "vat-direct-debit-setup-link",
            "Set up a Direct Debit",
            "http://localhost:8080/portal/vat/trader/vrn/directdebit?lang=eng",
            "VatDirectDebit:Click:Setup")

          doc.text() must not include "You've set up a Direct Debit to pay VAT"
          doc.text() must not include "We'll take payment for the period ending"
          doc.text() must not include "as long as you file your return on time."
        }
      }
      "have expandable content about direct debits when direct debit is eligible and active" in {

        val dDActive = Some(calendar.get.copy(directDebit = DirectDebit(ddiEligibilityInd = true,
          active = Some(DirectDebitActive(new LocalDate(2016, 6, 30),
            new LocalDate(2016, 8, 15)))
        )))

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), dDActive)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)

          doc.getElementById("vat-direct-debit-see-detail").text mustBe "You've set up a Direct Debit to pay VAT"

          doc.text() must include("We'll take payment for the period ending 30 June 2016" +
            " on 15 August 2016 as long as you file your return on time")

          assertLinkById(doc,
            "vat-direct-debit-help-link",
            "Change or cancel your Direct Debit",
            "http://localhost:9020/business-account/help/vat/direct-debit",
            "VatDirectDebit:Click:Help")
        }
      }
    }

    "there is an account summary to render and account balance is greater than zero" should {
      "show correct message with see breakdown link" in {
        val dueBalance = Some(AccountBalance(Some(BigDecimal(50.00))))

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = dueBalance), calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          doc.text() must include("You owe £50.00")
          assertLinkById(doc,
            "vat-see-breakdown-link",
            "see breakdown",
            "http://localhost:8080/portal/vat/trader/vrn/account/overview?lang=eng",
            "HomepageVAT:click:SeeBreakdown")

        }
      }
    }
    "there is an error retrieving the data" should {
      "return the generic error message" in {
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(VatGenericError))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          view.toString must include("We can’t display your VAT information at the moment.")
        }
      }
    }
    "the user has enrolment for Vat Var that is not activated" should {
      "have the correct message and link" in {
        val fakeRequestWithVatVarNotActivated: AuthenticatedRequest[AnyContent] = requestWithEnrolment(
          vatDecEnrolment, vatVarEnrolment.copy(isActivated = false))

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(VatNoData))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithVatVarNotActivated)) { result =>
          val doc = asDocument(result)
          doc.text() must include("Received an activation pin for Change Registration Details?")
          assertLinkById(doc,
            "vat-activate-or-enrol-details-summary",
            "Enter pin",
            "http://localhost:8080/portal/service/vat-change-details?action=activate&step=enteractivationpin&lang=eng&returnUrl=http%3A%2F%2Flocalhost%3A9020%2Fbusiness-account",
            "VATSummaryActivate:click:activate")
        }
      }
      "the user has no enrolment for Vat Var" should {
        "have the correct message and link" in {
          val fakeRequestWithVatVarNotActivated: AuthenticatedRequest[AnyContent] = requestWithEnrolment(
            vatDecEnrolment, VatNoEnrolment())

          reset(mockVatService)
          when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(VatNoData))
          whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithVatVarNotActivated)) { result =>
            val doc = asDocument(result)
            doc.text() must include("You're not set up to change VAT details online -")
            assertLinkById(doc,
              "vat-activate-or-enrol-details-summary",
              "set up now",
              "http://localhost:8080/portal/service/vat-change-details?action=enrol&step=enterdetails&lang=eng",
              "VATSummaryActivate:click:enrol")
          }
        }
      }
    }
  }
}