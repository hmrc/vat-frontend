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

package controllers.actions.mocks

import akka.stream.testkit.NoMaterializer
import config.FrontendAppConfig
import controllers.actions.mocks.MockAuth._
import controllers.actions.{AuthAction, AuthActionImpl, mocks}
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatNoEnrolment, VatVarEnrolment, Vrn}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Request, Result}
import play.api.test.StubPlayBodyParsersFactory
import uk.gov.hmrc.auth.core.AuthConnector

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockAuth extends MockitoSugar with BeforeAndAfterEach with StubPlayBodyParsersFactory {
  this: Suite =>

  final def testVrn: String = MockAuth.testVrn

  final def NoVatVar: UserType = mocks.UserType.NoVatVar

  final def InactiveVatVar: UserType = mocks.UserType.InactiveVatVar

  final def ActiveVatVar: UserType = mocks.UserType.ActiveVatVar

  final val mockAuthAction: AuthAction = spy(
    new AuthActionImpl(
      mock[AuthConnector],
      mock[FrontendAppConfig],
      stubPlayBodyParsers(NoMaterializer)
    )
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthAction)
  }

  final def mockAuth(userType: UserType = NoVatVar): Unit =
    doAnswer(
      new Answer[Future[Result]] {
        def answer(invocation: InvocationOnMock): Future[Result] = {
          val request: Request[_] = invocation.getArgument(0, classOf[Request[_]])
          val block: Request[_] => Future[Result] = invocation.getArgument(1, classOf[Request[_] => Future[Result]])

          val authenticated = userType match {
            case UserType.NoVatVar => noVatVar(request)
            case UserType.InactiveVatVar => inactiveVatVar(request)
            case UserType.ActiveVatVar => activeVatVar(request)
          }

          block(authenticated)
        }
      }
    ).when(mockAuthAction).invokeBlock(any(), any())

}

object MockAuth {
  val testVrn: String = UUID.randomUUID().toString

  def noVatVar[A](request: Request[A]): AuthenticatedRequest[A] =
    AuthenticatedRequest(request, "id", VatDecEnrolment(Vrn(testVrn), isActivated = true), VatNoEnrolment(), "credId")

  def inactiveVatVar[A](request: Request[A]): AuthenticatedRequest[A] =
    AuthenticatedRequest(request, "id", VatDecEnrolment(Vrn(testVrn), isActivated = true), VatVarEnrolment(Vrn(testVrn), isActivated = false), "credId")

  def activeVatVar[A](request: Request[A]): AuthenticatedRequest[A] =
    AuthenticatedRequest(request, "id", VatDecEnrolment(Vrn(testVrn), isActivated = true), VatVarEnrolment(Vrn(testVrn), isActivated = true), "credId")
}

sealed trait UserType

object UserType {

  case object NoVatVar extends UserType

  case object InactiveVatVar extends UserType

  case object ActiveVatVar extends UserType

}
