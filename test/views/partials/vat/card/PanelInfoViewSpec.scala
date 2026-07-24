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

package views.partials.vat.card

import play.api.inject.guice.GuiceApplicationBuilder
import views.ViewSpecBase
import views.html.partials.vat.card.panel_info

class PanelInfoViewSpec extends ViewSpecBase {

  class VatOutageContentOn extends ViewSpecBase {
    override def fakeApplication() = new GuiceApplicationBuilder()
      .configure(Map("microservice.services.features.vatOutageContent" -> true))
      .build()
  }

  "panel_info" when {

    "the VAT outage content feature is disabled" should {

      "not render the outage panel" in {
        val doc = asDocument(panel_info(None, frontendAppConfig, deferralPeriodOver = false)(messages))

        assertNotRenderedById(doc, "vat-card-panel-info1")
        doc.text() mustBe ""
      }
    }

    "the VAT outage content feature is enabled" should {

      "render the outage panel with the warning title and content" in new VatOutageContentOn {
        val doc = asDocument(panel_info(None, frontendAppConfig, deferralPeriodOver = false)(messages))

        assertRenderedById(doc, "vat-card-panel-info1")

        val text = doc.text()
        text must include(messages("bt.vat.card.vatOutageContentTitle"))
        text must include(messages("bt.vat.card.vatOutageContentp1").trim)
        text must include(messages("bt.vat.card.vatOutageContentp2").trim)
        text must include(messages("bt.vat.card.vatOutageContentp3").trim)
      }

      "render the outage panel regardless of the direct debit and deferral period arguments" in new VatOutageContentOn {
        val doc = asDocument(panel_info(Some(true), frontendAppConfig, deferralPeriodOver = true)(messages))

        assertRenderedById(doc, "vat-card-panel-info1")
      }
    }
  }
}