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
import connectors.MockHttpClient
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatEnrolment, VatNoEnrolment, VatVarEnrolment}
import org.joda.time.{DateTime, LocalDate}
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import utils.EmacUrlBuilder
import views.ViewSpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatVarPartialBuilderSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfter with MockHttpClient with ViewSpecBase{

  class testEnrolmentsStoreService(shouldShowNewPinLink: Boolean) extends EnrolmentsStoreService{
    def showNewPinLink(enrolment: VatEnrolment, currentDate: DateTime)(implicit hc: HeaderCarrier): Future[Boolean] = {
      Future(shouldShowNewPinLink)
    }
  }

  val request: Request[_] = FakeRequest()
  implicit val messagesToUse = messages
  val vatDecEnrolment = VatDecEnrolment(vrn = Vrn("testVrn"),isActivated = true)
  implicit val hc: HeaderCarrier = new HeaderCarrier()

  trait setupNoVatVal{
    implicit val authRequest: AuthenticatedRequest[_] = AuthenticatedRequest(request, "id", vatDecEnrolment, new VatNoEnrolment)
  }

  trait setupActiveVatVal{
    implicit val authRequest: AuthenticatedRequest[_] = AuthenticatedRequest(request, "id", vatDecEnrolment,
      new VatVarEnrolment(vrn = Vrn("testVrn"), isActivated = true))
  }

  trait setupInactiveVatVal{
    implicit val authRequest: AuthenticatedRequest[_] = AuthenticatedRequest(request, "id", vatDecEnrolment,
      new VatVarEnrolment(vrn = Vrn("testVrn"), isActivated = false))
  }

  "The vat var partial builder " should {
    "return the appropriate partial for a card when no vat var enrolment exists" in new setupNoVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(false), emacUrlBuilder, frontendAppConfig)
      val doc = builder.getPartialForCard().futureValue.get
      val parsedDoc = Jsoup.parse(doc.toString())
      parsedDoc.getElementById("change-vat-details-header").text() mustBe "Change VAT details online"
      assertLinkById(parsedDoc, "change-vat-details","Set up your VAT so you can change your details online",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account",
        expectedGAEvent = "link - click:Your business taxes cards:Set up your VAT so you can change your details online")
    }

    "return a null partial for a card when an activated vat var enrolment exists" in new setupActiveVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(false), emacUrlBuilder, frontendAppConfig)
      builder.getPartialForCard().futureValue mustBe None
    }

    "return the appropriate partial for a card when an unactivated vat var enrolment exists and it is within 7 days of application" in new setupInactiveVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(false), emacUrlBuilder, frontendAppConfig)
      val doc = builder.getPartialForCard().futureValue.get
      val parsedDoc = Jsoup.parse(doc.toString())
      parsedDoc.getElementById("change-vat-details-header").text() mustBe "Change VAT details online"
      parsedDoc.text() must include("We posted an activation code to you. Delivery takes up to 7 days.")
      assertLinkById(parsedDoc, "activate-vat-var", "Use the activation code so you can change your VAT details online",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http%3A%2F%2Flocalhost%3A9020%2Fbusiness-account",
        expectedGAEvent = "link - click:Your business taxes cards:change your VAT details online")
      parsedDoc.text() must include("It can take up to 72 hours to display your details.")
    }

    "return the appropriate partial for a card when an unactivated vat var enrolment exists and it is more than 7 days since application" in new setupInactiveVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(true), emacUrlBuilder, frontendAppConfig)
      val doc = builder.getPartialForCard().futureValue.get
      val parsedDoc = Jsoup.parse(doc.toString())
      parsedDoc.getElementById("change-vat-details-header").text() mustBe "Change VAT details online"
      parsedDoc.text() must include("Use the activation code we posted to you so you can")
      assertLinkById(parsedDoc, "activate-vat-var", "change your VAT details online",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=http%3A%2F%2Flocalhost%3A9020%2Fbusiness-account",
        expectedGAEvent = "link - click:Your business taxes cards:change your VAT details online")
      parsedDoc.text() must include("It can take up to 72 hours to display your details.")
      parsedDoc.text() must include("You can")
      assertLinkById(parsedDoc, "vat-var-new-code", "request a new activation code",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account",
        expectedGAEvent = "link - click:Your business taxes cards:Request a new vat var activation code")
    }

    "return the appropriate partial for the subpage when no vat var enrolment exists" in  new setupNoVatVal{
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(false), emacUrlBuilder, frontendAppConfig)
      val doc = builder.getPartialForSubpage().futureValue.get
      val parsedDoc = Jsoup.parse(doc.toString())
      parsedDoc.text() must include("You're not set up to change VAT details online -")
      assertLinkById(parsedDoc,
        "vat-activate-or-enrol-details-summary",
        "set up now (opens in a new window or tab)",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account",
        "link - click:VATVar:set up now",
        expectedIsExternal = true, expectedOpensInNewTab = true)
    }

    "return a null partial for the subpage when an activated vat var enrolment exists" in new setupActiveVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(false), emacUrlBuilder, frontendAppConfig)
      builder.getPartialForSubpage().futureValue mustBe None
    }

    "return the appropriate partial for the subpage when an unactivated vat var enrolment exists and it is within 7 days of application" in new setupInactiveVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(false), emacUrlBuilder, frontendAppConfig)
      val doc = builder.getPartialForSubpage().futureValue.get
      val parsedDoc = Jsoup.parse(doc.toString())
      parsedDoc.getElementById("change-vat-details-header").text() mustBe "Change VAT details online"
      parsedDoc.text() must include("We posted an activation code to you. Delivery takes up to 7 days.")
      assertLinkById(parsedDoc, "activate-vat-var", "Use the activation code so you can change your VAT details online",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=%2F",
        expectedGAEvent = "link - click:VATVar:Enter pin")
      parsedDoc.text() must include("It can take up to 72 hours to display your details.")
    }

    "return the appropriate partial for the subpage when an unactivated vat var enrolment exists and it is more than 7 days since application" in new setupInactiveVatVal {
      val builder = new VatVarPartialBuilderImpl(new testEnrolmentsStoreService(true), emacUrlBuilder, frontendAppConfig)
      val doc = builder.getPartialForSubpage().futureValue.get
      val parsedDoc = Jsoup.parse(doc.toString())
      parsedDoc.getElementById("change-vat-details-header").text() mustBe "Change VAT details online"
      parsedDoc.text() must include("Use the activation code we posted to you so you can")
      assertLinkById(parsedDoc, "activate-vat-var", "change your VAT details online",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/get-access-tax-scheme?continue=%2Fbusiness-account&returnUrl=%2F",
        expectedGAEvent = "link - click:VATVar:Enter pin")
      parsedDoc.text() must include("It can take up to 72 hours to display your details.")
      parsedDoc.text() must include("You can")
      assertLinkById(parsedDoc, "vat-var-new-code", "request a new activation code",
        "/enrolment-management-frontend/HMCE-VATVAR-ORG/request-new-activation-code?continue=%2Fbusiness-account",
        expectedGAEvent = "link - click:VATVar:Lost pin")
    }


  }
}
