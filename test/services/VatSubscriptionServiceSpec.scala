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

package services

import scala.concurrent.Future

import base.SpecBase
import connectors.{MockHttpClient, VatSubscriptionConnector}
import connectors.models._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

class VatSubscriptionServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfter with MockHttpClient {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockVatSubscriptionConnector: VatSubscriptionConnector = mock[VatSubscriptionConnector]
  val service = new VatSubscriptionService(mockVatSubscriptionConnector)
  val vatEnrolment: Vrn = Vrn("vrn")

  before {
    reset(mockVatSubscriptionConnector)
  }

  "The VatSubscriptionService mandationStatus method" when {

    "the connector returns a mandation status" should {

      "return a boolean" in {

        when(mockVatSubscriptionConnector.mandationStatus(vatEnrolment))
          .thenReturn(Future.successful(Some(MandationStatus("MTDfB Mandated"))))

        whenReady(service.mandationStatus(vatEnrolment)) {
          _ mustBe true
        }
      }
    }

    "the connector returns a None" should {

      "return false" in {

        when(mockVatSubscriptionConnector.mandationStatus(vatEnrolment))
          .thenReturn(Future.successful(None))

        whenReady(service.mandationStatus(vatEnrolment)) {
          _ mustBe false
        }
      }
    }
  }
}
