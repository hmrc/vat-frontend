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

package views.partials.account_summary.vat.vat_var

import models.VatDecEnrolment
import play.twirl.api.Html
import uk.gov.hmrc.domain.Vrn
import views.behaviours.ViewBehaviours
import views.html.partials.account_summary.vat.vat_var.vat_var_prompt_to_activate

class VatVarPromptToActivateSpec extends ViewBehaviours {

  val enrolment = VatDecEnrolment(Vrn(""), true)

  def view: Html =
    vat_var_prompt_to_activate(frontendAppConfig, emacUrlBuilder, enrolment, "returnUrl")(fakeRequest, messages)

  "view" should {
    val doc = asDocument(view)

    "have header" in {
      assertContainsText(doc, "Change VAT registration details")
    }

    "have contain text" in {
      assertContainsText(doc, "You'll need your activation code to do this.")
    }

    "have lost pin link" in {
      assertLinkById(
        doc,
        linkId = "vat-activate-or-enrol-details-summary-lost-pin",
        expectedText = "I've lost my activation code",
        expectedUrl = "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account",
        expectedGAEvent = "link - click:VATVar:Lost pin"
      )
    }

    "have activate link" in {
      assertLinkById(
        doc,
        linkId = "vat-activate-or-enrol-details-summary",
        expectedText = "Activate Activate Change VAT registration details",
        expectedUrl = "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=returnUrl",
        expectedGAEvent = "link - click:VATVar:Enter pin"
      )
    }
  }
}