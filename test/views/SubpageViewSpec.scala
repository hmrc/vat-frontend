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

package views

import models.VatDecEnrolment
import org.scalatest.mockito.MockitoSugar
import play.twirl.api.Html
import uk.gov.hmrc.domain.Vrn
import views.behaviours.ViewBehaviours
import views.html.subpage


class SubpageViewSpec extends ViewBehaviours with MockitoSugar {

  def messageKeyPrefix = "subpage"

  def test_service_info = Html("<p id=\"partial-content\">This is service info</p>")
  def test_account_summary = Html("<p>This is an account summary.</p>")
  def test_sidebar = Html("<p>This is a sidebar.</p>")

  def testVRN = "testVRN"
  def testVatDecEnrolment = VatDecEnrolment(Vrn(testVRN), true)

  def createView = () => subpage(frontendAppConfig, test_account_summary, test_sidebar, testVatDecEnrolment)(test_service_info)(fakeRequest, messages)
  def doc = asDocument(createView())

  "the aggregated subpage " should {
    behave like normalPage(createView, messageKeyPrefix)
    "display the supplied account summary" in {
      doc.text() must include("This is an account summary.")
    }

    "display the supplied sidebar" in {
      doc.text() must include("This is a sidebar.")
    }

    "have the supplied service info" in {
      doc.getElementById("partial-content").text mustBe "This is service info"
    }

    "include the warning about the time to show paymnets" in {
      doc.text() must include("Payments will take 4 to 7 working days to show on this page.")
      doc.text() must include("Completed return amounts will take 1 to 2 days.")
    }

    "include the 'Submitted returns' heading" in {
      doc.text() must include("Submitted returns")
    }

    "include the 'view submitted returns' link" in {
      assertLinkById(doc, "view-submitted-returns","View submitted returns (opens in a new window or tab)",s"http://localhost:8080/portal/vat-file/trader/$testVRN/periods?lang=eng",
        "link - click:VATPreviouslySubmittedReturns:View submitted returns", expectedIsExternal = true, expectedOpensInNewTab = true )
    }

    "include the 'correct a mistake' link" in {
      assertLinkById(doc, "correct-a-mistake","Correct a mistake (opens in a new window or tab)","https://www.gov.uk/vat-corrections",
        "link - click:VATPreviouslySubmittedReturns:Correct a mistake", expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "include the 'Payments and repayments' heading" in {
      doc.text() must include("Payments and repayments")
    }

    "include the 'view payments and repayments' link" in {
      assertLinkById(doc, "view-payments-and-repayments","View payments and repayments (opens in a new window or tab)",
        s"http://localhost:8080/portal/vat/trader/$testVRN/account/overview?lang=eng",
        "link - click:VATPaymentsAndRepayments:View payments and repayments",
        expectedIsExternal = true, expectedOpensInNewTab = true )
    }

    "include the 'vat certificate' link" in {
      doc.text() must include ("Repayments are made to the account stated on your")
      assertLinkById(doc, "vat-certificate","VAT certificate (opens in a new window or tab)",s"http://localhost:8080/portal/vat/trader/$testVRN/certificate?lang=eng",
        "link - click:VATPaymentsAndRepayments:VAT certificate",
        expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "include the 'change repayments account' link" in {
      assertLinkById(doc, "change-repayments-account","Change your repayments account (opens in a new window or tab)",
        s"http://localhost:8080/portal/vat-variations/org/$testVRN/introduction?lang=eng",
        "link - click:VATPaymentsAndRepayments:Change your repayments account",
        expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "show the 'Get filing reminders' link" in {
      assertLinkById(doc, "get-filing-reminders", "Get filing reminders (opens in a new window or tab)",
        "https://foo.hmrc.gov.uk/eprompt/httpssl/changeVatEmailAddress.do","link - click:VATMoreOptions:Get filing reminders",
        expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "show the 'View VAT certificate' link" in {
      assertLinkById(doc, "view-vat-certificate", "View VAT certificate (opens in a new window or tab)",
        s"http://localhost:8080/portal/vat/trader/$testVRN/certificate?lang=eng","link - click:VATMoreOptions:View VAT certificate",
        expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "show the 'Paying by Direct Debit' link" in {
      assertLinkById(doc, "paying-by-direct-debit", "Paying by Direct Debit",
        "http://localhost:9733/business-account/help/vat/how-to-pay","link - click:VATMoreOptions:Paying by Direct Debit")

    }

    "show the 'Add a VAT service' link" in {
      assertLinkById(doc, "add-vat-service", "Add a VAT service, e.g EC Sales List",
        "http://localhost:9020/business-account/add-tax/vat","link - click:VATMoreOptions:Add a VAT service")
    }
  }

}
