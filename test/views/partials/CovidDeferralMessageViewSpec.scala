/*
 * Copyright 2022 HM Revenue & Customs
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
import views.html.partials.covid_deferral_message

class CovidDeferralMessageViewSpec extends ViewSpecBase{

  "covid_deferral_message" should {
    "display the correct content when the deferral period is ending" in {
      def view = () => covid_deferral_message(false)(messages)

      asDocument(view()).text() must include("We previously set out that you could delay (defer) paying VAT because of coronavirus (COVID-19). The VAT deferral period ends on 30 June 2020.")
      asDocument(view()).text() must include("If you cancelled your Direct Debit, set it up again so you do not miss a payment. Contact our Payment Support Service as soon as possible if you cannot pay. You might be able to set up a Time to Pay agreement if you're struggling to pay a tax bill.")
      asDocument(view()).text() must include("You still need to submit VAT Returns, even if your business has temporarily closed.")
      asDocument(view()).text() must include("You have until 31 March 2021 to pay VAT bills that were due between 20 March 2020 and 30 June 2020.")

      assertLinkById(asDocument(view()), "difficulties-paying-link",
        "Payment Support Service",
        "https://www.gov.uk/difficulties-paying-hmrc")
    }

    "display the correct content when the deferral period has ended" in {
      def view = () => covid_deferral_message(true)(messages)

      asDocument(view()).text() must include("We previously set out that you could delay (defer) paying VAT because of coronavirus (COVID-19). The VAT deferral period ended on 30 June 2020.")
      asDocument(view()).text() must include("If you cancelled your Direct Debit, set it up again so you do not miss a payment. Contact our Payment Support Service as soon as possible if you cannot pay. You might be able to set up a Time to Pay agreement if you're struggling to pay a tax bill.")
      asDocument(view()).text() must include("You still need to submit VAT Returns, even if your business has temporarily closed.")
      asDocument(view()).text() must include("You have until 31 March 2021 to pay VAT bills that were due between 20 March 2020 and 30 June 2020.")

      assertLinkById(asDocument(view()), "difficulties-paying-link",
        "Payment Support Service",
        "https://www.gov.uk/difficulties-paying-hmrc")
    }
  }

}
