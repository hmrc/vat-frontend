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

package views.partials.vat

import models.{VatDecEnrolment, Vrn}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.partials.vat.vat_activate_address_tab

class VatActivateAddressTabViewSpec extends ViewBehaviours {

  val vatDecEnrolment = VatDecEnrolment(Vrn("a-users-vrn"), isActivated = true)

  def createView: () => HtmlFormat.Appendable =
    () => vat_activate_address_tab(emacUrlBuilder, vatDecEnrolment)(fakeRequest, messages)

  "Vat enrol address tab partial" should {

    "display correct content" in {
      asDocument(createView()).getElementById("vat-activate-address-tab").text() must include("You can't change your address yet.")
    }

    val enrolLink = asDocument(createView()).getElementById("vat-activate-or-enrol-details-address")

    "display correct link text" in {
      enrolLink.text() must include("Activate change VAT details")
    }

    "display correct link href when not allowed" in {
      enrolLink.attr("href") must include(
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account"
      )
    }
  }
}
