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
import com.typesafe.config.{Config, ConfigFactory}
import config.VatHeaderCarrierForPartialsConverter
import connectors.ServiceInfoPartialConnector
import models.requests.{AuthenticatedRequest, ServiceInfoRequest}
import models.{VatDecEnrolment, VatNoEnrolment}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCryptoProvider

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class ServiceInfoActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val testConnector:ServiceInfoPartialConnector = mock[ServiceInfoPartialConnector]
  when(testConnector.getServiceInfoPartial()(Matchers.any()))thenReturn(Future.successful(Html("testHtml")))

  val testConfig: Config = ConfigFactory.parseMap(
    Map(
      "cookie.encryption.key"         -> "gvBoGdgzqG1AarzF1LY0zQ==",
      "sso.encryption.key"            -> "gvBoGdgzqG1AarzF1LY0zQ==",
      "queryParameter.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
      "json.encryption.key"           -> "gvBoGdgzqG1AarzF1LY0zQ=="
    ).asJava
  )

  val testAppCrypto: ApplicationCrypto = new ApplicationCrypto(testConfig)
  val testHeaderCarrier = new VatHeaderCarrierForPartialsConverter(new SessionCookieCryptoProvider(testAppCrypto).get())

  class TestableAction(connector:ServiceInfoPartialConnector,
                       headerCarrier: VatHeaderCarrierForPartialsConverter) extends ServiceInfoActionImpl(connector, headerCarrier) {
    def testTransform[A](request: AuthenticatedRequest[A]): Future[ServiceInfoRequest[A]] = {
      transform(request)
    }
  }

  "The service info action's transform method" should {
    "inject the html returned by the connector into the request" in {
      val actionUnderTest: TestableAction = new TestableAction(testConnector, testHeaderCarrier)

      val actionResult = actionUnderTest.testTransform(new AuthenticatedRequest[AnyContentAsEmpty.type](fakeRequest,"testId",
        VatDecEnrolment(Vrn("testVrn"), true), VatNoEnrolment(), "credId"))
      val transformedRequest = Await.result(actionResult,Duration(60,"s"))
      transformedRequest.serviceInfoContent mustBe Html("testHtml")
    }
  }

}
