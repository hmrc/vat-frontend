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

package models

import utils.RadioOption

case class VatNotAddedFormModel (
  radioOption: Option[String]
)

object VatNotAddedFormModel {

  val options: Seq[RadioOption] = Seq(
    RadioOption("add-vat", "add_your_vat_to_this_account", "unauthorised.add_your_vat_to_this_account"),
    RadioOption("sign-in", "sign_in_to_other_account", "unauthorised.sign_in_to_other_account")
  )

}
