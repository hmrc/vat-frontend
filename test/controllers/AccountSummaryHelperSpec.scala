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

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)
  def requestWithEnrolment(activated: Boolean, vatVarEnrolment: VatEnrolment = VatNoEnrolment()): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated), vatVarEnrolment)
  }

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(activated = true)

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockVatService, messagesApi)

  "getAccountSummaryView" when {
    "there is an empty account summary" should {
      "show a complete return button and correct message" in {
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(VatNoData))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          doc.getElementById("vat-file-return-link").text mustBe "Complete your VAT return (opens in HMRC online)"
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
      "have expandable content about repayments" in {

        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(
          VatData(accountSummary.copy(accountBalance = creditBalance), calendar)
        ))

        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { result =>
          val doc = asDocument(result)
          val expandableContent = doc.getElementsByClass("panel-indent").text

          doc.getElementById("vat-when-repaid").text mustBe "When you'll be repaid"

          expandableContent must include("We''ll transfer this amount to your repayments bank account if you''ve set one up." +
                                         " We''ll post you a payable order (like a cheque) otherwise.")
          expandableContent must include("We normally send payment within 10 days unless we need to make checks," +
                                         " for example if you''re reclaiming more VAT than usual.")
          expandableContent must include("Don''t get in touch unless you''ve been in credit for more than 21 days.")

          assertLinkById(doc,
            "vat-repayments-account",
            "repayments bank account",
            "",
            "VAT:click:RepaymentsBankAccount")
          assertLinkById(doc,
            "vat-more-than-21-days",
            "more than 21 days",
            "",
            "VAT:click:MoreThan21Days")
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
  }
}