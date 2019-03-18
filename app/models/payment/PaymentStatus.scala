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

package models.payment

import play.api.libs.json._

sealed trait PaymentStatus {
  val value: String
}

object Created extends PaymentStatus {
  val value = "created"
}

object Successful extends PaymentStatus {
  val value = "successful"
}

object Sent extends PaymentStatus {
  val value = "sent"
}

object Failed extends PaymentStatus {
  val value = "failed"
}

object Cancelled extends PaymentStatus {
  val value = "cancelled"
}

object Invalid extends PaymentStatus {
  val value = "invalid"
}

object PaymentStatus {

  def paymentStatusReads: Reads[PaymentStatus] = new Reads[PaymentStatus] {
    override def reads(json: JsValue): JsResult[PaymentStatus] = {
      json match {
        case JsString(x) => {
          x.toLowerCase match {
            case Successful.value => JsSuccess(Successful)
            case Created.value => JsSuccess(Created)
            case _ => JsSuccess(Invalid) //don't throw error if status doesn't match
          }
        }
        case _ => JsError()
      }
    }
  }

  def paymentStatusWrites: Writes[PaymentStatus] = new Writes[PaymentStatus] {
    def writes(x: PaymentStatus): JsValue = JsString(x.value)
  }
}