/*
 * Copyright 2026 HM Revenue & Customs
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

/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatestplus.play._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

class FormHelpersSpec extends PlaySpec {

  object TestFormProvider {
    val form: Form[String] = Form(
      "name" -> text.verifying(nonEmpty)
    )
  }

  "FormHelpers.getErrorByKey" should {

    "return error message when error exists" in {
      val boundForm = TestFormProvider.form.bind(Map("name" -> "")) // triggers validation error

      val result = FormHelpers.getErrorByKey(boundForm, "name")

      result mustBe "error.required" // default Play message key for nonEmpty
    }

    "return empty string when no error exists" in {
      val boundForm = TestFormProvider.form.bind(Map("name" -> "TestName"))

      val result = FormHelpers.getErrorByKey(boundForm, "name")

      result mustBe ""
    }

    "return empty string when key does not exist" in {
      val boundForm = TestFormProvider.form.bind(Map("name" -> "")) // error on "name"

      val result = FormHelpers.getErrorByKey(boundForm, "unknownKey")

      result mustBe ""
    }
  }
}
