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

package utils

import identifiers.StopId
import models.Stop
import play.api.mvc.Call

trait NextPage[A, B] {
  def get(b: B): Call
}

object NextPage {

  implicit val stop: NextPage[StopId.type, Stop] = {
    new NextPage[StopId.type, Stop] {
      override def get(b: Stop): Call = {
        b match {
          case Stop.Dormant => Call("GET", "https://www.gov.uk/dormant-company/dormant-for-corporation-tax")
          case Stop.Close => Call("GET", "https://www.gov.uk/closing-a-limited-company")
        }
      }
    }
  }
}