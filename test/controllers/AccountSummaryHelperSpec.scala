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

package controllers

import base.SpecBase
import connectors.models.{AccountSummaryData, VatModel}
import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import services.VatService
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import views.html.subpage2

import scala.concurrent.Future
import scala.util.Success

class AccountSummaryHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures with SpecBase {

  //TODO: Needs AccountSummaryData
  val accountSummary = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  val mockHelper = mock[Helper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any())).thenReturn(Future.successful(accountSummary))

  val mockVatService: VatService = mock[VatService]

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockVatService, messagesApi)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)
  def requestWithEnrolment(activated: Boolean): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment())
  }

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(activated = true)

  def viewAsString(balanceInformation: String = ""): String =
    subpage2(accountSummary, "", frontendAppConfig, mockHelper)(HtmlFormat.empty)(fakeRequestWithEnrolments, messages, requestWithEnrolment(true)).toString

  "getAccountSummaryView" when {

  }

  def requestWithEnrolment(activated: Boolean, vatVarEnrolment: VatEnrolment = VatNoEnrolment()): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated), vatVarEnrolment)
  }
  var testUrl = "www.test.url"

  "the account summary helper" when {
    "retrieving the VAT Vars view for a user with no Vat Var enrolment " should {
      "Display the message and link to set up VAT details" in {
        implicit val requestWithoutVatVar = requestWithEnrolment(true)
        whenReady(accountSummaryHelper.getVatVarsActivationView(testUrl)){ view =>
          view.toString must include ("You're not set up to change VAT details online")
          val doc =  asDocument(view)
          assertLinkById (
            doc, "vat-activate-or-enrol-details-summary", "set up now",
            "http://localhost:8080/portal/service/vat-change-details?action=enrol&step=enterdetails&lang=eng",
            "VATSummaryActivate:click:enrol"
          )
        }
      }
    }

    "retrieving the VAT Vars view for a user who has an unactivated enrolment for VAT Var" should {
      "Display the message and link to activate" in {
        implicit val requestWithUnactivatedVatVar = requestWithEnrolment(true,VatVarEnrolment(Vrn("vrn"), isActivated = false))
        whenReady(accountSummaryHelper.getVatVarsActivationView(testUrl)){ view =>
            view.toString must include ("Received an activation pin for Change Registration Details?")
            val doc =  asDocument(view)
            assertLinkById(doc, "vat-activate-or-enrol-details-summary", "Enter pin",
              s"http://localhost:8080/portal/service/vat-change-details?action=activate&step=enteractivationpin&lang=eng&returnUrl=$testUrl",
                "VATSummaryActivate:click:activate")
          }
       }
    }

    "retrieving the VAT Vars view for a user who has an activated enrolment for VAT Var" should{
      "Not show a link associated with enrolling for or activating VAT var" in {
          implicit val requestWithActivatedVatVar = requestWithEnrolment(true,VatVarEnrolment(Vrn("vrn"), isActivated = true))
          whenReady(accountSummaryHelper.getVatVarsActivationView(testUrl)){ view =>
              val doc =  asDocument(view)
              doc.getElementById("vat-activate-or-enrol-details-summary") mustBe null
            }
        }
    }
  }
}
