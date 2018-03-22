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

package controllers.helpers

import base.SpecBase
import connectors.models.{CalendarData, DirectDebit}
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import models.requests.AuthenticatedRequest
import play.api.test.FakeRequest

class SidebarHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures with SpecBase {

  val testVrn = "testVrn"
  val testVatDecEnrolment = VatDecEnrolment(Vrn("testVrn"),true)
  val testSidebarHelper = new SidebarHelper(frontendAppConfig, messagesApi)
  val testQuarterlyJanAprJulOct = Some(Calendar(filingFrequency = Quarterly(January)))
  val testQuarterlyMarJunSepDec = Some(Calendar(filingFrequency = Quarterly(March)))
  val testQuarterlyFebMayAugNov = Some(Calendar(filingFrequency = Quarterly(February)))
  val testMonthly = Some(Calendar(filingFrequency = Monthly))
  val testAnnually = Some(Calendar(filingFrequency = Annually))

  implicit val testAuthRequest = AuthenticatedRequest(FakeRequest(),"externalId", testVatDecEnrolment, VatNoEnrolment())
  "The sidebar" when{
    "there is a user" should {
      "show the user's VRN " in {
        val view = testSidebarHelper.buildSideBar(None)
        view.toString must include ("VAT registration number (VRN)")
        view.toString must include ("testVrn")
      }

      "show the 'When you file for VAT' header" in  {
         val view = testSidebarHelper.buildSideBar(None)
        view.toString must include ("When you file for VAT")
      }

      "show the 'More options' header" in {
        val view = testSidebarHelper.buildSideBar(None)
        view.toString must include ("More options")
      }

      "show the 'Get filing reminders' link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "get-filing-reminders", "Get filing reminders",
          "https://foo.hmrc.gov.uk/eprompt/httpssl/changeVatEmailAddress.do","VatSubpage:click:GetFilingReminders")
      }

      "show the 'View VAT certificate' link" in {
        val view =testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "view-vat-certificate", "View VAT certificate",
          s"http://localhost:8080/portal/vat/trader/$testVrn/certificate?lang=eng","VatSubpage:click:ViewVatCertificate")
      }

      "show the 'Paying by Direct Debit' link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "paying-by-direct-debit", "Paying by Direct Debit",
          "http://localhost:9020/business-account/help/vat/direct-debit","VatSubpage:click:DirectDebits")

      }

      "show the 'Add a VAT service' link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "add-vat-service", "Add a VAT service, e.g EC Sales List",
          "http://localhost:9020/business-account/add-tax/vat","VatSubpage:click:AddService")
      }

      "show the 'Help and contact' link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "help-and-contact", "Help and contact",
          "http://localhost:9020/business-account/help/vat","VatSubpage:click:HelpAndContact")
      }

      "show the Deregister link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "deregister-vat", "Deregister for VAT",
          "/business-account/vat/deregister","VatSubpage:click:DeregisterVat")
      }

      "show the More link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "more-vat-options", "More",
          s"http://localhost:8080/portal/vat/trader/$testVrn?lang=eng","VatSubpage:click:MoreOptions")
      }

    }

    "the user's calendar information is missing" should {
      "show the error message with the link to the VAT certificate page" in  {
        val view = testSidebarHelper.buildSideBar(None)

        val doc = asDocument(view)
        doc.text() must include ("We can't display this at the moment")
        doc.text() must include ("Try again later, or check 'frequency of returns' on")
        assertLinkById(doc, "your-vat-certificate", "your VAT certificate",
          "http://localhost:8080/portal/vat/trader/testVrn/certificate?lang=eng","")
      }
    }

    "the user files anually" should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testAnnually)

        val doc = asDocument(view)
        doc.text() must include ("You file once a year.")
        assertLinkById(doc, "leave-annual-scheme", "Leave the VAT annual accounting scheme", "https://www.gov.uk/vat-annual-accounting-scheme/join-or-leave-the-scheme" , "VatSubpage:click:LeaveTheVatAnnualAccountingScheme" )
        doc.text() must include ("(to file quarterly)")

      }
    }

    "the user files monthly" should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testMonthly)

        val doc = asDocument(view)
        doc.text() must include ("You file monthly.")
        assertLinkById(doc, "change-to-quarterly", "Change to quarterly filing", "https://www.gov.uk/vat-annual-accounting-scheme/overview" , "VatSubpage:click:ChangeToQuarterlyFiling" )
        assertLinkById(doc, "change-to-annual", "Change to annual filing", "https://www.gov.uk/vat-annual-accounting-scheme/overview", "VatSubpage:click:ChangeToAnnualFiling")
      }
    }

    "the user files quarterly in March, Jun etc." should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testQuarterlyMarJunSepDec)

        val doc = asDocument(view)
        doc.text() must include ("You file every 3 months for periods ending March, June, September and December.")
        assertLinkById(doc, "change-to-monthly", "File monthly or change filing months", s"http://localhost:8080/portal/vat-variations/org/$testVrn/introduction?lang=eng" , "VatSubpage:click:FileMonthlyOrChangeFilingMonths" )
        assertLinkById(doc, "change-to-annual", "Change to annual filing", "https://www.gov.uk/vat-annual-accounting-scheme/overview", "VatSubpage:click:ChangeToAnnualFiling")

      }
    }

    "the user files quarterly in January, April etc." should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testQuarterlyJanAprJulOct)
        val doc = asDocument(view)
        doc.text() must include ("You file every 3 months for periods ending January, April, July and October.")
        assertLinkById(doc, "change-to-monthly", "File monthly or change filing months", s"http://localhost:8080/portal/vat-variations/org/$testVrn/introduction?lang=eng" , "VatSubpage:click:FileMonthlyOrChangeFilingMonths" )
        assertLinkById(doc, "change-to-annual", "Change to annual filing", "https://www.gov.uk/vat-annual-accounting-scheme/overview", "VatSubpage:click:ChangeToAnnualFiling")
      }
    }

    "the user files quarterly in February, May etc." should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testQuarterlyFebMayAugNov)
        val doc = asDocument(view)
        doc.text() must include ("You file every 3 months for periods ending February, May, August and November.")
        assertLinkById(doc, "change-to-monthly", s"File monthly or change filing months", s"http://localhost:8080/portal/vat-variations/org/$testVrn/introduction?lang=eng" , "VatSubpage:click:FileMonthlyOrChangeFilingMonths" )
        assertLinkById(doc, "change-to-annual", "Change to annual filing", "https://www.gov.uk/vat-annual-accounting-scheme/overview", "VatSubpage:click:ChangeToAnnualFiling")
      }
    }

  }
}
