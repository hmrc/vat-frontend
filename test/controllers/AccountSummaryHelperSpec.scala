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

import base.SpecBase
import connectors.models._
import models._
import models.requests.AuthenticatedRequest
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import views.html.subpage2

import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

class AccountSummaryHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures with SpecBase {

  //TODO: Needs AccountSummaryData
  val accountSummary = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  val mockHelper = mock[Helper]
  when(mockAccountSummaryHelper.getVatModel(Matchers.any())).thenReturn(Future.successful(accountSummary))

  val mockVatService: VatService = mock[VatService]

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockVatService, messagesApi)

  val calendarFileMonthly = new CalendarData(Some("0000"),DirectDebit(true,None),None, Seq())

  val vatModelNoData = VatModel(Success(None), None)
  val vatModelZeroBalance = VatModel(Success(Some(AccountSummaryData(Some(AccountBalance(Some(0.0))),None))),None)
  def vatModelSumOwed(owed: BigDecimal, calendar:Option[CalendarData] = None) =
    VatModel(Success(Some(AccountSummaryData(Some(AccountBalance(Some(owed))),None))),calendar)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)
  def requestWithEnrolment(activated: Boolean, vatVarEnrolment: VatEnrolment = VatNoEnrolment()): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated), vatVarEnrolment)
  }

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(activated = true)
  val testUrl = "ThisIsATestURL"

  when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelNoData))

  def viewAsString(balanceInformation: String = "", authRequest: AuthenticatedRequest[_] = fakeRequestWithEnrolments): String =
    subpage2(accountSummary, "", frontendAppConfig, mockHelper)(HtmlFormat.empty)(fakeRequestWithEnrolments, messages, requestWithEnrolment(true)).toString

  "getAccountSummaryView" when {
    "the user has no enrolment for VAT Var" should{
      "Display the message and link to set up VAT details" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)){ view =>
          view.toString must include ("You're not set up to change VAT details online")
          val doc =  asDocument(view)
          assertLinkById(doc, "vat-activate-or-enrol-details-summary", "set up now",
            frontendAppConfig.getPortalUrl("vat-change-details-enrol")(None),"VATSummaryActivate:click:enrol")
        }
      }
    }

    "the user has an unactivated enrolment for VAT Var" should{
      "Display the message and link to activate" in {
        implicit val requestWithUnactivatedVatVar = requestWithEnrolment(true,VatVarEnrolment(Vrn("vrn"), isActivated = false))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)){ view =>
          view.toString must include ("Received an activation pin for Change Registration Details?")
          val doc =  asDocument(view)
          assertLinkById(doc, "vat-activate-or-enrol-details-summary", "Enter pin",
            frontendAppConfig.getPortalUrl("vat-change-details")(Some(requestWithUnactivatedVatVar.vatVarEnrolment)) + "&" + frontendAppConfig.getReturnUrl(testUrl),
            "VATSummaryActivate:click:activate")
        }
      }
    }

    "the user has an activated enrolment for VAT Var" should{
      "Not show a link associated with enrolling for or activating VAT var" in {
        implicit val requestWithActivatedVatVar = requestWithEnrolment(true,VatVarEnrolment(Vrn("vrn"), isActivated = true))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)){ view =>
          val doc =  asDocument(view)
          doc.getElementById("vat-activate-or-enrol-details-summary") mustBe null
        }
      }
    }

    "the user has no account summary" should {
      "show 'No balance information to display'" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelNoData))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)){ view =>
          view.toString must include ("No balance information to display")
        }
      }

      "show the 'File a Return' button" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelNoData))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          val doc = asDocument(view)
          assertLinkById(doc, "vat-file-return-link", "Complete your VAT return (opens in HMRC online)",
            frontendAppConfig.getPortalUrl("vatFileAReturn")(Some(requestWithoutVatVar.vatDecEnrolment)),
            "vat:Click:Submit your VAT return")
        }
      }
    }

    "the user has a balance of zero" should {
      "Show 'You have nothing to pay' and the link to the statement" in  {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelZeroBalance))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          view.toString must include ("You have nothing to pay")
          val doc = asDocument(view)
          assertLinkById(doc, "vat-see-breakdown-link", "view statement",
            frontendAppConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(requestWithoutVatVar.vatDecEnrolment)),
            "HomepageVAT:click:SeeBreakdown")
        }
      }
    }

    "The user owes money " should {
      "Show the sum owed" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelSumOwed(100)))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          view.toString must include("You owe")
          view.toString must include("&pound;100.00")
        }
      }

      "Show the link to the breakdown" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelSumOwed(100)))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          val doc = asDocument(view)
          assertLinkById(doc, "vat-see-breakdown-link", "see breakdown",
            frontendAppConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(requestWithoutVatVar.vatDecEnrolment)),
            "HomepageVAT:click:SeeBreakdown")
        }
      }
    }

    "The user is owed money and files monthly" should {
      "Show the credit amount" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelSumOwed(-100)))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          view.toString must include("You are")
          view.toString must include("&pound;100.00")
          view.toString must include("in credit")
        }
      }

      "Show the link to the breakdown" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelSumOwed(-100)))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          val doc = asDocument(view)
          assertLinkById(doc, "vat-see-breakdown-link", "see breakdown",
            frontendAppConfig.getPortalUrl("vatPaymentsAndRepayments")(Some(requestWithoutVatVar.vatDecEnrolment)),
            "HomepageVAT:click:SeeBreakdown")
        }
      }

      "Show the 'When you'll be repaid' content" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        reset(mockVatService)
        when(mockVatService.fetchVatModel(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(vatModelSumOwed(-100,
          Some(calendarFileMonthly))))
        whenReady(accountSummaryHelper().getAccountSummaryView(testUrl)) { view =>
          view.toString() must include("When you'll be repaid")
          val doc = asDocument(view)
          //###TODO - Complete this test


        }
      }
    }
  }
}
