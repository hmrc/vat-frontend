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

package connectors

import base.SpecBase
import connectors.models._
import org.mockito.Matchers
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.CtUtr
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class VatConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with MockHttpClient {
  

  def ctConnector[A](mockedResponse: HttpResponse, httpWrapper: HttpWrapper = mock[HttpWrapper]): VatConnector = {
    when(httpWrapper.getF[A](Matchers.any())).
      thenReturn(mockedResponse)
    new VatConnector(http(httpWrapper), frontendAppConfig)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val ctUtr = CtUtr("utr")

  "CtConnector account summary" should {

    "call the micro service with the correct uri and return the contents" in {

    }

    "call the micro service with the correct uri and return no contents if there are none" in {

    }

    "call the micro service and return 500" in {

    }
  }

  val sampleDesignatoryDetails =
    """{

      |}""".stripMargin

  "CtConnector designatory details" should {

    "Return the correct response for an example with designatory details information" in {


    }

    "call the micro service and return 500" in {


    }

  }


}
