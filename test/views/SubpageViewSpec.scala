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

import connectors.models.{AccountSummaryData, VatModel}
import models.requests.AuthenticatedRequest
import models.{Helper, VatDecEnrolment, VatNoEnrolment}
import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Vrn
import views.behaviours.ViewBehaviours
import views.html.subpage2

import scala.util.Success

class SubpageViewSpec extends ViewBehaviours with MockitoSugar {

  val vatModel = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val messageKeyPrefix = "subpage"
  val currentUrl = ""
  val vrn = Vrn("this-is-a-vrn")
  val mockHelper = mock[Helper]

  def vatEnrolment(activated: Boolean = true) =  VatDecEnrolment(vrn, isActivated = activated)

  def authenticatedRequest = AuthenticatedRequest(FakeRequest(), "", vatEnrolment(true), VatNoEnrolment())

  def createView = () => subpage2(vatModel, currentUrl, frontendAppConfig, mockHelper)(Html("<p id=\"partial-content\">hello world</p>"))(fakeRequest, messages, authenticatedRequest)

  "Subpage view" must {
    behave like normalPage(createView, messageKeyPrefix)
    "contain correct content" in {
      val doc = asDocument(createView())
      doc.getElementsByTag("h1").first().text() mustBe "VAT details"
      doc.getElementById("payments-notice").text() mustBe
        "Information Payments will take 4 to 7 working days to show on this page. Completed return amounts will take 1 to 2 days."
    }

    "render the provided partial content" in {
      val doc = asDocument(createView()).getElementById("partial-content").text mustBe "hello world"
    }
  }

  "Subpage sidebar" must {

    "exist" in {
      assertRenderedByTag(asDocument(createView()), "aside")
    }

    "contain the user's VRN" in {
      val vrnBlock = asDocument(createView()).getElementById("vat-vrn")
      vrnBlock.text() mustBe "VAT registration number (VRN) this-is-a-vrn"
    }

    "contain the 'When you file for VAT' sub section" in {
      val doc = asDocument(createView())
      doc.getElementById("filing-for-vat").getElementsByTag("h3").text() mustBe "When you file for VAT"
      assertLinkById(doc, "file-monthly", "File monthly or change filing months", "https://www.gov.uk/dormant-company/dormant-for-corporation-tax", expectedGAEvent = "CtSubpage:click:FileMonthlyOrChangeFilingMonths")
      assertLinkById(doc, "file-annually", "Change to annual filing", "https://www.gov.uk/closing-a-limited-company", expectedGAEvent = "CtSubpage:click:ChangeToAnnualFiling")
    }

  }
}
