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

package controllers.helpers

import base.SpecBase
import models._
import models.requests.AuthenticatedRequest
import org.jsoup.nodes.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase

class SidebarHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures with SpecBase {

  val testVrn = "testVrn"
  val testVatDecEnrolment = VatDecEnrolment(Vrn("testVrn"),true)
  val testSidebarHelper = new SidebarHelper(frontendAppConfig, messagesApi)
  val testQuarterlyJanAprJulOct = Some(Calendar(filingFrequency = Quarterly(January)))
  val testQuarterlyMarJunSepDec = Some(Calendar(filingFrequency = Quarterly(March)))
  val testQuarterlyFebMayAugNov = Some(Calendar(filingFrequency = Quarterly(February)))
  val testMonthly = Some(Calendar(filingFrequency = Monthly))
  val testAnnually = Some(Calendar(filingFrequency = Annually))
  val testInvalidStaggerCode = Some(Calendar(filingFrequency = InvalidStaggerCode))

  implicit val testAuthRequest = AuthenticatedRequest(FakeRequest(),"externalId", testVatDecEnrolment, VatNoEnrolment())
  "The sidebar" when{
    "there is a user" should {
      "show the user's VRN " in {
        val view = testSidebarHelper.buildSideBar(None)
        view.toString must include ("VAT registration number (VRN):")
        view.toString must include ("testVrn")
      }

      "show the 'When you file for VAT' header" in  {
         val view = testSidebarHelper.buildSideBar(None)
        view.toString must include ("When you file for VAT")
      }

      "show the 'Help and contact' link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "help-and-contact", "Help and contact",
          "http://localhost:9733/business-account/help","link - click:VATsidebar:Help and contact")
      }

      "show the 'Online seminars' link" in {
        val view = testSidebarHelper.buildSideBar(None)
        assertLinkById(asDocument(view), "online-seminars", "Online seminars to learn about tax (opens in a new window or tab)",
          "https://www.gov.uk/government/collections/hmrc-webinars-email-alerts-and-videos",
          "link - click:VATsidebar:Online seminars to learn about tax", expectedIsExternal = true, expectedOpensInNewTab = true)
      }

    }

    "the user's calendar information is missing" should {
      "show the error message with the link to the VAT certificate page" in  {
        val view = testSidebarHelper.buildSideBar(None)

        val doc = asDocument(view)
        doc.text() must not include ("We can't display this at the moment")
        doc.text() must include ("You can view the frequency of your returns on your")
        assertLinkById(doc, "your-vat-certificate", "VAT certificate (opens in a new window or tab)",
          "http://localhost:8080/portal/vat/trader/testVrn/certificate?lang=eng","link - click:VATsidebar:VAT certificate",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }
    }

    "the user files anually" should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testAnnually)

        val doc = asDocument(view)
        doc.text() must include ("You file once a year.")
        assertLinkById(doc, "leave-annual-scheme", "Leave the VAT annual accounting scheme (opens in a new window or tab)",
          "https://www.gov.uk/vat-annual-accounting-scheme/join-or-leave-the-scheme" ,
          "link - click:VATsidebar:leave the VAT annual accounting scheme", expectedIsExternal = true, expectedOpensInNewTab = true )
        doc.text() must include ("to file quarterly.")

      }
    }

    "the user files monthly" should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testMonthly)

        val doc = asDocument(view)
        doc.text() must include ("You file monthly.")
        assertLinkById(doc, "change-to-quarterly", "Change to quarterly filing (opens in a new window or tab)",
          s"http://localhost:8080/portal/vat-variations/org/$testVrn/introduction?lang=eng",
          "link - click:VATsidebar:Change to quarterly filing",
          expectedIsExternal = true, expectedOpensInNewTab = true)
        assertLinkById(doc, "change-to-annual", "Change to annual filing (opens in a new window or tab)",
          "https://www.gov.uk/vat-annual-accounting-scheme/overview", "link - click:VATsidebar:Change to annual filing",
          expectedIsExternal = true, expectedOpensInNewTab = true)
      }
    }


    def validateLinksForQuarterlyFiling(doc: Document) = {
      assertLinkById(doc, "change-to-monthly", "File monthly or change filing months (opens in a new window or tab)",
        s"http://localhost:8080/portal/vat-variations/org/$testVrn/introduction?lang=eng",
        "link - click:VATsidebar:File monthly or change filing months",
        expectedIsExternal = true, expectedOpensInNewTab = true)
      assertLinkById(doc, "change-to-annual", "Change to annual filing (opens in a new window or tab)",
        "https://www.gov.uk/vat-annual-accounting-scheme/overview", "link - click:VATsidebar:Change to annual filing",
        expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "the user files quarterly in March, Jun etc." should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testQuarterlyMarJunSepDec)

        val doc = asDocument(view)
        doc.text() must include ("You file every 3 months for periods ending March, June, September and December.")
        validateLinksForQuarterlyFiling(doc)
      }
    }


    "the user files quarterly in January, April etc." should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testQuarterlyJanAprJulOct)
        val doc = asDocument(view)
        doc.text() must include ("You file every 3 months for periods ending January, April, July and October.")
        validateLinksForQuarterlyFiling(doc)
      }
    }

    "the user files quarterly in February, May etc." should {
      "show the filing period of the user" in {
        val view = testSidebarHelper.buildSideBar(testQuarterlyFebMayAugNov)
        val doc = asDocument(view)
        doc.text() must include ("You file every 3 months for periods ending February, May, August and November.")
        validateLinksForQuarterlyFiling(doc)
      }
    }
    "the stagger code is invalid" should {
      "return calendar missing view" in {
        val view = testSidebarHelper.buildSideBar(testInvalidStaggerCode)
        val doc = asDocument(view)
        val docText = asDocument(view).text()

        docText must not include ("We can't display this at the moment.")
        docText must include ("You can view the frequency of your returns on your")
        assertLinkById(
          doc,
          "your-vat-certificate",
          "VAT certificate (opens in a new window or tab)",
          "http://localhost:8080/portal/vat/trader/testVrn/certificate?lang=eng",
          "link - click:VATsidebar:VAT certificate",
          expectedIsExternal = true, expectedOpensInNewTab = true)

      }
    }
  }
}
