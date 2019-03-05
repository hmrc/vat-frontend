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

package services

import base.SpecBase
import connectors.models.{AccountSummaryData, OpenPeriod, VatData}
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatEnrolment, VatVarEnrolment}
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase

class ReturnsPartialBuilderSpec extends ViewSpecBase {

  implicit val messagesToUse: Messages = messages
  val vatDecEnrolment = VatDecEnrolment(Vrn("vrn"), isActivated = true)
  val vatVarEnrolment = VatVarEnrolment(Vrn("vrn"), isActivated = true)

  def requestWithEnrolment(vatDecEnrolment: VatDecEnrolment, vatVarEnrolment: VatEnrolment): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vatDecEnrolment, vatVarEnrolment)
  }

  implicit val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(vatDecEnrolment, vatVarEnrolment)

  "The Returns partial builder" should{

    val testBuilder = new ReturnsPartialBuilder(frontendAppConfig)

    val testDataNoReturns = new VatData( new AccountSummaryData(None, None), None)
    val testDataOneReturn = new VatData( new AccountSummaryData(None, None, Seq(OpenPeriod(DateTime.now.toLocalDate))), None)
    val testDataTwoReturns = new VatData( new AccountSummaryData(None, None, Seq(OpenPeriod(DateTime.now.toLocalDate),
      OpenPeriod(DateTime.now.minusMonths(1).toLocalDate))), None)

    val testEnrolment = new VatEnrolment {override val isActivated: Boolean = true
      override val vrn: Vrn = Vrn("123456789")
    }


    "return the expected partial when there are no returns to complete" in {
      val partial = Jsoup.parse(testBuilder.buildReturnsPartial(testDataNoReturns, testEnrolment).toString())
      partial.text() must include("You have no returns to complete")
      assertLinkById(partial, "vat-view-previous-returns", "View previous VAT Returns",
        "http://localhost:8080/portal/vat-file/trader/123456789/periods?lang=eng",
        expectedGAEvent = "link - click:VAT cards:View previous VAT Returns", expectedIsExternal = true,
        expectedOpensInNewTab = true)
      assertLinkById(partial, "vat-amend-return", "Amend a mistake in a VAT Return", "https://www.gov.uk/vat-corrections",
        expectedGAEvent = "link - click:VAT cards:Amend a Return", expectedIsExternal = true,
        expectedOpensInNewTab = true)
    }

    "return the expected partial when there is one return to complete" in {
      val partial = Jsoup.parse(testBuilder.buildReturnsPartial(testDataOneReturn, testEnrolment).toString())
      partial.text() must include("A VAT Return is ready to complete")
      assertLinkById(partial, "vat-complete-return", "Complete VAT Return",
        "http://localhost:8080/portal/vat-file/trader/123456789/return?lang=eng",
        expectedGAEvent = "link - click:VAT cards:Complete VAT Return", expectedIsExternal = true,
        expectedOpensInNewTab = true)
    }

    "return the expected partial when there are multiple returns to complete" in {
      val partial = Jsoup.parse(testBuilder.buildReturnsPartial(testDataTwoReturns, testEnrolment).toString())
      partial.text() must include("2 VAT Returns are ready to complete")
      assertLinkById(partial, "vat-complete-returns", "Complete VAT Returns",
        "http://localhost:8080/portal/vat-file/trader/123456789/return?lang=eng",
        expectedGAEvent = "link - click:VAT cards:Complete VAT Returns", expectedIsExternal = true,
        expectedOpensInNewTab = true)
    }

  }
}
