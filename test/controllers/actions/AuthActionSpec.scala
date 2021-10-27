/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.actions

import base.SpecBase
import controllers.actions.AuthActionSpec._
import controllers.routes._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

object AuthActionSpec {

  implicit class AuthUtil[A](val input: A) extends AnyVal {
    def ~[B](input2: B): ~[A, B] = new ~(input, input2)
  }

}

class AuthActionSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[AuthConnector].toInstance(mockAuthConnector)
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  class Harness extends FrontendController(inject[MessagesControllerComponents]) {
    def onPageLoad(): Action[AnyContent] = inject[AuthAction].apply { _ => Ok }
  }

  val testRetrievedCredentials: Option[Credentials] = Some(Credentials(providerId = "credId", providerType = "type"))

  type RetrievalType = ~[~[Some[String], Enrolments], Option[Credentials]]

  def mockAuth(result: Future[RetrievalType]): Unit =
    when(mockAuthConnector.authorise[RetrievalType](any(), any())(any(), any())).thenReturn(result)

  val SUT = new Harness

  "Auth Action" when {
    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        mockAuth(Future.failed(MissingBearerToken()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        mockAuth(Future.failed(BearerTokenExpired()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        mockAuth(Future.failed(InsufficientEnrolments()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(UnauthorisedController.onPageLoad().url)
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {
        mockAuth(Future.failed(InsufficientConfidenceLevel()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(UnauthorisedController.onPageLoad().url)
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {
        mockAuth(Future.failed(UnsupportedAuthProvider()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {
        mockAuth(Future.failed(UnsupportedAffinityGroup()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the unauthorised page" in {
        mockAuth(Future.failed(UnsupportedCredentialRole()))

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(UnauthorisedController.onPageLoad().url)
      }
    }

    val vatEnrolment = Enrolment("HMCE-VATDEC-ORG", Seq(EnrolmentIdentifier("VATRegNo", "vat-vrn")), "Activated")
    val mtdVatEnrolment = Enrolment("HMRC-MTD-VAT")

    "the user has a valid enrolment" must {
      "return 200" in {
        val retrievalResult: Future[RetrievalType] =
          Future.successful(Some("foo") ~ Enrolments(Set(vatEnrolment)) ~ testRetrievedCredentials)
        mockAuth(retrievalResult)

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }
    }

    "the user has an MTD VAT enrolment" must {
      "redirect to the MTD homepage" in {
        val retrievalResult: Future[RetrievalType] =
          Future.successful(Some("foo") ~ Enrolments(Set(mtdVatEnrolment)) ~ testRetrievedCredentials)
        mockAuth(retrievalResult)

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:9152/vat-through-software/vat-overview")
      }
    }

    "the user has an MTD VAT enrolment and a VAT-DEC enrolment" must {
      "redirect to the MTD homepage" in {
        val retrievalResult: Future[RetrievalType] =
          Future.successful(Some("foo") ~ Enrolments(Set(vatEnrolment, mtdVatEnrolment)) ~ testRetrievedCredentials)
        mockAuth(retrievalResult)

        val result = SUT.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:9152/vat-through-software/vat-overview")
      }
    }
  }

}
