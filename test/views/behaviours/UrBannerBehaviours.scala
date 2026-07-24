/*
 * Copyright 2026 HM Revenue & Customs
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

package views.behaviours

import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait UrBannerBehaviours extends ViewSpecBase {

  def nonLinearPage(view: () => HtmlFormat.Appendable): Unit = {
    urBannerPage(view)

    "the button should be visible" in {
      val doc = asDocument(view())
      assertRenderedByClass(doc, "hmrc-user-research-banner__close")
    }
  }

  def linearPage(view: () => HtmlFormat.Appendable): Unit = {
    urBannerPage(view)

    "the button should be hidden" in {
      val doc = asDocument(view())
      assertNotRenderedByCssSelector(doc, ".hmrc-user-research-banner__close")
    }
  }

  private def urBannerPage(view: () => HtmlFormat.Appendable): Unit = {

    "behave like a page with the User Research Banner" must {
      "have the banner element" in {
        val doc = asDocument(view())
        assertRenderedByClass(doc, "hmrc-user-research-banner")
      }

      "have the correct title element" in {
        val doc = asDocument(view())
        assertRenderedByClass(doc, "hmrc-user-research-banner__title")
      }

      "have the correct link element" in {
        val doc = asDocument(view())
        assertRenderedByClass(doc, "hmrc-user-research-banner__link")
      }
    }
  }

}
