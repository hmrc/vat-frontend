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

import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatNoEnrolment}
import org.scalatest.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Vrn
import views.behaviours.ViewBehaviours
import views.html.partial

class PartialViewSpec extends ViewBehaviours with MockitoSugar {

  val messageKeyPrefix = "partial"

  val fakeSummary = Html("<p>This is the account summary</p>")

  override lazy val app = new GuiceApplicationBuilder().configure(
    Map("microservice.services.features.changes-to-vat" -> true)
  ).build()

  def vatEnrolment(activated: Boolean = true) = VatDecEnrolment(Vrn("vrn"), isActivated = true)

  def authenticatedRequest = AuthenticatedRequest(FakeRequest(), "", vatEnrolment(), VatNoEnrolment())

  def createView: () => HtmlFormat.Appendable =
    () => partial(Vrn("VRN"), frontendAppConfig, fakeSummary)(fakeRequest, messages, authenticatedRequest)

  "Partial view" must {
    "pass the title" in {
      asDocument(createView()).text() must include("VAT")
    }

    "pass the vrn of the user" in {
      asDocument(createView()).text() must include("VAT registration number (VRN)")
    }

    "pass the account summary partial" in {
      asDocument(createView()).html() must include(fakeSummary.toString())
    }

    "have a more details link" in {
      assertLinkById(asDocument(createView()), "vat-details-link", "More VAT details",
        s"${frontendAppConfig.getUrl("mainPage")}", "link - click:VATpartial:more VAT details")
    }

    "pass the main heading regarding changes to VAT" in {
      asDocument(createView()).text() must include("Changes to VAT")
    }

    "pass the subheading regarding changes to VAT" in {
      asDocument(createView()).text() must include("Use software to submit your VAT Returns")
    }

    "pass the detail regarding changes to VAT" in {
      asDocument(createView()).text() must include("From April 2019, VAT registered businesses with a turnover above " +
        "£85,000 must use relevant third party software to submit their VAT Returns.")
    }

    "have a find out more link" in {
      assertLinkById(asDocument(
        createView()), "changes-to-vat-link", "Find out more about changes to VAT", frontendAppConfig.changesToVatUrl,
        "link - click:VATpartial:find out more about changes to VAT"
      )
    }
  }
}
