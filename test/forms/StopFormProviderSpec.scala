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

package forms

import forms.behaviours.FormBehaviours
import models._

class StopFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "value" -> Stop.options.head.value
  )

  val form = new StopFormProvider()()

  "Stop form" must {

    behave like questionForm[Stop](Stop.values.head)

    behave like formWithOptionField(
      Field(
        "value",
        Required -> "stop.error.required",
        Invalid -> "error.invalid"),
      Stop.options.toSeq.map(_.value): _*)
  }
}
