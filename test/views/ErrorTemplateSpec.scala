/*
 * Copyright 2024 HM Revenue & Customs
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

import views.behaviours.ViewBehaviours
import views.html.error_template

class ErrorTemplateSpec extends ViewBehaviours {
  "the error template" should{
    val res = inject[error_template].apply("testTitle", "testHeading", "testMessage", frontendAppConfig)(fakeRequest,messages)
    "show the passed content" in {
      asDocument(res).getElementsByTag("h1").first.text() mustBe "testHeading"
      assertEqualsMessage(asDocument(res), "title", "testTitle")
      asDocument(res).text() must include("testMessage")
    }

    "have the correct banner title" in {
      val doc = asDocument(res)
      val nav = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
      nav.text mustBe "Business tax account"
    }

    "display language toggles" in {
      val doc = asDocument(res)
      assertRenderedByClass(doc, "hmrc-language-select__list")
    }

    "display the sign out link" in {
      val doc = asDocument(res)
      assertLinkByClass(doc, "govuk-link hmrc-sign-out-nav__link", "Sign out", "http://localhost:9020/business-account/sso-sign-out")
    }
  }

}
