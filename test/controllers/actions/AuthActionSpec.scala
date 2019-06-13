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

package controllers.actions

import base.SpecBase
import controllers.actions.AuthActionSpec._
import controllers.routes
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Controller
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object AuthActionSpec {

  implicit class AuthUtil[A](val input: A) extends AnyVal {
    def ~[B](input2: B): ~[A, B] = new ~(input, input2)
  }

}

class AuthActionSpec extends SpecBase with MockitoSugar {

  class Harness(authAction: AuthAction) extends Controller {
    def onPageLoad() = authAction { request => Ok }
  }

  val testRetrievedCredentials: Option[Credentials] = Some(Credentials(providerId = "credId", providerType = "type"))

  "Auth Action" when {
    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new MissingBearerToken), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new BearerTokenExpired), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new InsufficientEnrolments), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new UnsupportedAuthProvider), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthActionImpl(new FakeFailingAuthConnector(new UnsupportedCredentialRole), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    val vatEnrolment = Enrolment("HMCE-VATDEC-ORG", Seq(EnrolmentIdentifier("VATRegNo", "vat-vrn")), "Activated")
    val mtdVatEnrolment = Enrolment("HMRC-MTD-VAT")

    "the user has a valid enrolment" must {
      "return 200" in {
        val retrievalResult: Future[~[~[Option[String], Enrolments], Option[Credentials]]] =
          Future.successful(Some("foo") ~ Enrolments(Set(vatEnrolment)) ~ testRetrievedCredentials)

        val authAction = new AuthActionImpl(new FakeSuccessfulAuthConnector(retrievalResult), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "the user has an MTD VAT enrolment" must {
      "redirect to the MTD homepage" in {
        val retrievalResult: Future[~[~[Some[String], Enrolments], Option[Credentials]]] =
          Future.successful(Some("foo") ~ Enrolments(Set(mtdVatEnrolment)) ~ testRetrievedCredentials)

        val authAction = new AuthActionImpl(new FakeSuccessfulAuthConnector(retrievalResult), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:9152/vat-through-software/vat-overview")
      }
    }

    "the user has an MTD VAT enrolment and a VAT-DEC enrolment" must {
      "redirect to the MTD homepage" in {
        val retrievalResult: Future[~[~[Some[String], Enrolments], Option[Credentials]]] =
          Future.successful(Some("foo") ~ Enrolments(Set(vatEnrolment, mtdVatEnrolment)) ~ testRetrievedCredentials)

        val authAction = new AuthActionImpl(new FakeSuccessfulAuthConnector(retrievalResult), frontendAppConfig)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:9152/vat-through-software/vat-overview")
      }
    }
  }
}

class FakeSuccessfulAuthConnector(retrievalResult: Future[_]) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    retrievalResult.map(_.asInstanceOf[A])
}

class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
