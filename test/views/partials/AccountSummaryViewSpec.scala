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

package views.partials

import views.ViewSpecBase
import views.html.partials.account_summary

class AccountSummaryViewSpec extends ViewSpecBase {
  def view = () => account_summary("hello world", frontendAppConfig)(fakeRequest, messages)

  "Account summary" when {
    "there is a user" should {
      "display the link to file a return (cato)" in {
        assertLinkById(asDocument(view()),
          "ct-file-return-cato", "Complete Corporation Tax return", "/cato",
          "corporation-tax:Click:Send your corporation tax")
      }

      "display the heading and link to make a payment" in {
        asDocument(view()).getElementsByTag("h2").first().text() mustBe "Your payments"
        assertLinkById(asDocument(view()),
          "ct-make-payment-link", "Make a Corporation Tax payment", "http://localhost:9050/pay-online/corporation-tax/make-a-payment?mode=bta",
          "corporation-tax:Click:Make a CT payment")
      }

      "render the provided balance information" in {
        asDocument(view()).getElementsByTag("p").first().text() must include("hello world")
      }
    }
  }
}
