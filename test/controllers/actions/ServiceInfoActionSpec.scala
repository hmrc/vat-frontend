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
import com.typesafe.config.{Config, ConfigFactory}
import config.VatHeaderCarrierForPartialsConverter
import connectors.ServiceInfoPartialConnector
import controllers.ServiceInfoController
import models.{VatDecEnrolment, VatNoEnrolment, Vrn}
import models.requests.{AuthenticatedRequest, ServiceInfoRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCryptoProvider

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ServiceInfoActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val testConnectorController: ServiceInfoController = mock[ServiceInfoController]
  val testConnector: ServiceInfoPartialConnector = mock[ServiceInfoPartialConnector]

  val testConfig: Config = ConfigFactory.parseMap(
    Map(
      "cookie.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
      "sso.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
      "queryParameter.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
      "json.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ=="
    ).asJava
  )

  val testAppCrypto: ApplicationCrypto = new ApplicationCrypto(testConfig)
  val testHeaderCarrier = new VatHeaderCarrierForPartialsConverter(new SessionCookieCryptoProvider(testAppCrypto).get())

  class TestableAction(serviceInfoController: ServiceInfoController) extends ServiceInfoActionImpl(serviceInfoController) {
    def testTransform[A](request: AuthenticatedRequest[A]): Future[ServiceInfoRequest[A]] = {
      transform(request)
    }
  }
  "The service info action's transform method" should {
    "inject the html returned by the connector into the request" in {
      when(testConnectorController.serviceInfoPartial(any())(any(),any())).thenReturn(Future.successful(Some(Html("testHtml"))))

      val actionUnderTest: TestableAction = new TestableAction(testConnectorController)

      val actionResult = actionUnderTest.testTransform(new AuthenticatedRequest[AnyContent](fakeRequest, "testId",
        VatDecEnrolment(Vrn("testVrn"), true), VatNoEnrolment(), "credId"))

      val transformedRequest = await(actionResult)


      transformedRequest.serviceInfoContent mustBe Html("testHtml")
    }
  }

}
