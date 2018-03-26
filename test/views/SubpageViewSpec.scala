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

  "the aggregated subpage " should {
    behave like normalPage(createView, messageKeyPrefix)
    "display the supplied account summary" in {
      asDocument(createView()).text() must include("This is an account summary.")
    }

    "display the supplied sidebar" in {
      asDocument(createView()).text() must include("This is a sidebar.")
    }

    "have the supplied service info" in {
      asDocument(createView()).getElementById("partial-content").text mustBe "This is service info"
    }

    "include the warning about the time to show paymnets" in {
      asDocument(createView()).text() must include("Payments will take 4 to 7 working days to show on this page.")
      asDocument(createView()).text() must include("Completed return amounts will take 1 to 2 days.")
    }

    "include the 'Previously submitted returns' heading" in {
      asDocument(createView()).text() must include("Previously submitted returns")
    }

    "include the 'view submitted returns' link" in {
      val doc = asDocument(createView())
      assertLinkById(doc, "view-submitted-returns","View submitted returns",s"http://localhost:8080/portal/vat-file/trader/$testVRN/periods?lang=eng",
        "VatSubpage:click:ViewSubmittedReturn" )
    }

    "include the 'correct a mistake' link" in {
      val doc = asDocument(createView())
      assertLinkById(doc, "correct-a-mistake","Correct a mistake","http://localhost:9020/business-account/help/vat/correct-a-mistake",
        "VatSubpage:click:CorrectAMistake" )
    }

    "include the 'Payments and repayments' heading" in {
      asDocument(createView()).text() must include("Payments and repayments")
    }

    "include the 'view payments and repayments' link" in {
      val doc = asDocument(createView())
      assertLinkById(doc, "view-payments-and-repayments","View payments and repayments",
        s"http://localhost:8080/portal/vat/trader/$testVRN/account/overview?lang=eng",
        "VatSubpage:click:ViewPaymentsAndRepayments" )
    }

    "include the 'vat certificate' link" in {
      val doc = asDocument(createView())
      doc.text() must include ("Repayments are made to the account stated on your")
      assertLinkById(doc, "vat-certificate","VAT certificate",s"http://localhost:8080/portal/vat/trader/$testVRN/certificate?lang=eng",
        "VatSubpage:click:ViewRepaymentsOnVatCertificate" )
    }

    "include the 'change repayments account' link" in {
      val doc = asDocument(createView())
      assertLinkById(doc, "change-repayments-account","Change your repayments account",
        s"http://localhost:8080/portal/vat-variations/org/$testVRN/introduction?lang=eng",
        "VatSubpage:click:ChangeYourRepaymentsAccount" )
    }
  }

}
