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

import models.CtEnrolment
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.CtUtr
import views.behaviours.ViewBehaviours
import views.html.subpage

class SubpageViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "subpage"

  val utr = CtUtr("this-is-a-utr")
  val ctEnrolment = CtEnrolment(utr, isActivated = true)

  def createView = () => subpage(frontendAppConfig, ctEnrolment, Html("<p id=\"partial-content\">hello world</p>"))(HtmlFormat.empty)(fakeRequest, messages)

  "Subpage view" must {
    behave like normalPage(createView, messageKeyPrefix)
    "contain correct content" in {
      val doc = asDocument(createView())
      doc.getElementsByTag("h1").first().text() mustBe "Corporation Tax details"
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

    "contain the users UTR" in {
      val utrBlock = asDocument(createView()).getElementById("ct-utr")
      utrBlock.text() mustBe "Unique Taxpayer Reference (UTR) this-is-a-utr"
    }

    "contain the stop trading links" in {
      val doc = asDocument(createView())
      doc.getElementById("stop-trading").getElementsByTag("h3").text() mustBe "Stop trading"
      assertLinkById(doc, "make-dormant", "Make your company dormant", "https://www.gov.uk/dormant-company/dormant-for-corporation-tax", expectedGAEvent = "CtSubpage:click:MakeDormant")
      assertLinkById(doc, "close-company", "Close your company", "https://www.gov.uk/closing-a-limited-company", expectedGAEvent = "CtSubpage:click:Close")
    }

    "contain the more options links" in {
      val doc = asDocument(createView())
      doc.getElementById("more-options").getElementsByTag("h3").text() mustBe "More options"
      assertLinkById(doc, "cert-of-residence", "Get a certificate of residence", "https://www.gov.uk/guidance/get-a-certificate-of-residence", expectedGAEvent = "CtSubpage:click:CertificateOfResidence")
      assertLinkById(doc, "setup-partnership", "Set up a partnership or add a partner", "/forms/form/register-a-partner-or-a-partnership-for-self-assessment/new", expectedGAEvent = "CtSubpage:click:SetupPartnership")
      assertLinkById(doc, "help-and-contact", "Help and contact", "http://localhost:9020/business-account/help/corporation-tax/questions", expectedGAEvent = "CtSubpage:click:HelpAndContact")
      assertLinkById(doc, "more", "More", s"http://localhost:8080/portal/corporation-tax/org/$utr/account/balanceperiods?lang=eng", expectedGAEvent = "CtSubpage:click:MoreOptions")

    }
  }
}
