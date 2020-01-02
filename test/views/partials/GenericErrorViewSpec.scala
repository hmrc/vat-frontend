/*
 * Copyright 2020 HM Revenue & Customs
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
import views.html.partials.account_summary.vat.generic_error

class GenericErrorViewSpec extends ViewSpecBase {
  def view = () => generic_error("http://portal.url")(fakeRequest, messages)

  "Generic error" should {
    "display the correct content" in {
      asDocument(view()).text() must include("We can’t display your VAT information at the moment.")
      asDocument(view()).text() must include("Try refreshing the page in a few minutes or use the old HMRC website (opens in a new window or tab).")
      assertLinkById(asDocument(view()), "vat-old-hmrc", "old HMRC website (opens in a new window or tab)",
        "http://portal.url", "link - click:VATgenericError:old HMRC website", expectedIsExternal = true, expectedOpensInNewTab = true)
    }
  }
}
