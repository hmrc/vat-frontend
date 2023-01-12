/*
 * Copyright 2023 HM Revenue & Customs
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

package models.payment

import models.payment.PaymentStatus.{ApiStatusCode, Invalid, Successful}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, JsSuccess, Json}

class PaymentStatusSpec extends AnyWordSpec with Matchers {

  val invalidStatuses: Set[String] = Set(ApiStatusCode.Created, ApiStatusCode.Sent, ApiStatusCode.Failed, ApiStatusCode.Cancelled, "anything else")

  "paymentStatusReads" should {
    "parse successful as Successful" in {
      PaymentStatus.paymentStatusReads.reads(JsString(ApiStatusCode.Successful)) mustBe JsSuccess(Successful)
    }
    "parse everything else as invalid" in {
      for (status <- invalidStatuses) {
        PaymentStatus.paymentStatusReads.reads(JsString(status)) mustBe JsSuccess(Invalid)
      }
    }
  }

  "paymentStatusWrites" should {
    "write Successful as the SuccessfulJsString" in {
      Json.toJson[PaymentStatus](Successful)(PaymentStatus.paymentStatusWrites) mustBe JsString(ApiStatusCode.Successful)
    }
    "write Invalid as the invalid JsString" in {
      Json.toJson[PaymentStatus](Invalid)(PaymentStatus.paymentStatusWrites) mustBe JsString(ApiStatusCode.Invalid)
    }
  }

}
