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
import connectors.VatConnector
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

class VatServiceSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  val mockVatConnector: VatConnector = mock[VatConnector]

  val service = new VatService(mockVatConnector)

  val vatEnrolment = VatDecEnrolment(Vrn("utr"), isActivated = true)

  "The VatService designatoryDetails method" when {
    "the connector returns designatory details" should {
      "return <TYPE>" in {

      }
    }
    "the connector returns an exception" should {
      "return None when designatoryDetails call throws an exception" in {

      }
    }
  }
}
