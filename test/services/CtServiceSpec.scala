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

import base.SpecBase
import connectors.CtConnector
import connectors.models.{CtAccountBalance, CtAccountSummaryData, CtDesignatoryDetailsCollection}
import models._
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.CtUtr
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CtServiceSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  val mockCtConnector: CtConnector = mock[CtConnector]

  val service = new CtService(mockCtConnector)

  val ctEnrolment = CtEnrolment(CtUtr("utr"), isActivated = true)

  val ctAccountSummary = CtAccountSummaryData(Some(CtAccountBalance(Some(1200.0))))

  "The CtService fetchCtModel method" when {
    "the connector return data" should {
      "return CtData" in {
        reset(mockCtConnector)
        when(mockCtConnector.accountSummary(ctEnrolment.ctUtr)).thenReturn(Future.successful(Option(ctAccountSummary)))

        whenReady(service.fetchCtModel(Some(ctEnrolment))) {
          _ mustBe CtData(ctAccountSummary)
        }
      }
    }
    "the connector returns no data" should {
      "return CtNotFoundError" in {
        reset(mockCtConnector)
        when(mockCtConnector.accountSummary(ctEnrolment.ctUtr)).thenReturn(Future.successful(None))

        whenReady(service.fetchCtModel(Some(ctEnrolment))) {
          _ mustBe CtNoData
        }
      }
    }
    "the connector throws an exception" should {
      "return CtGenericError" in {
        reset(mockCtConnector)
        when(mockCtConnector.accountSummary(ctEnrolment.ctUtr)).thenReturn(Future.failed(new Throwable))

        whenReady(service.fetchCtModel(Some(ctEnrolment))) {
          _ mustBe CtGenericError
        }
      }
    }
    "the ct enrolment is empty" should {
      "return a CtEmpty" in {
        reset(mockCtConnector)

        whenReady(service.fetchCtModel(None)) {
          _ mustBe CtEmpty
        }
      }
    }
    "the ct enrolment is not activated" should {
      "return a CtUnactivated" in {
        reset(mockCtConnector)

        whenReady(service.fetchCtModel(Some(CtEnrolment(CtUtr("utr"), isActivated = false)))) {
          _ mustBe CtUnactivated
        }
      }
    }
  }

  "The CtService designatoryDetails method" when {
    "the connector returns designatory details" should {
      "return CtDesignatoryDetailsCollection" in {
        val designatoryDetails = Some(CtDesignatoryDetailsCollection(None, None))
        when(mockCtConnector.designatoryDetails(ctEnrolment.ctUtr)).thenReturn(Future.successful(designatoryDetails))

        whenReady(service.designatoryDetails(ctEnrolment)) {
          _ mustBe designatoryDetails
        }
      }
    }
    "the connector returns an exception" should {
      "return None when designatoryDetails call throws an exception" in {
        when(mockCtConnector.designatoryDetails(ctEnrolment.ctUtr)).thenReturn(Future.failed(new Throwable))

        whenReady(service.designatoryDetails(ctEnrolment)) {
          _ mustBe None
        }
      }
    }
  }
}
