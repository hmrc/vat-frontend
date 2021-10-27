/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import forms.VatNotAddedForm
import models.VatNotAddedFormModel
import play.api.data.Form
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.whichAccountAddVat

class WhichAccountAddVatViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "unauthorised.account_to_add_vat"
  val vatNotAddedForm: VatNotAddedForm = inject[VatNotAddedForm]

  val validData: Map[String, String] = Map(
    "value" -> VatNotAddedFormModel.options.head.value
  )
  val invalidData: Map[String, String] = Map(
    "radioOption" -> "this_no_good_option"
  )

  val form: Form[VatNotAddedFormModel] = vatNotAddedForm.form
  val formWithValidData: Form[VatNotAddedFormModel] = form.bind(validData)
  val formWithInvalidData: Form[VatNotAddedFormModel] = form.bind(invalidData)

  def createViewUsingForm: Form[VatNotAddedFormModel] => Html =
    (form: Form[VatNotAddedFormModel]) =>
      inject[whichAccountAddVat].apply(formWithValidData, frontendAppConfig)(
        fakeRequest,
        messages
      )

  def view(): Html =
    inject[whichAccountAddVat].apply(form, frontendAppConfig)(fakeRequest, messages)

  "Which Account To Add VAT view" must {

    behave like normalPage(view, messageKeyPrefix)

    "contain heading ID" in {
      val doc = asDocument(view())
      doc.getElementsByTag("h1").attr("id") mustBe "account-to-add-vat"
    }

    "have the correct content" in {
      val doc = asDocument(view())
      doc.text() must include("Which account do you want to add VAT?")
      doc.text() must include("Sign into your other account to add VAT")
      doc.text() must include("Add VAT to this account")
    }

  }

  "Which Account To Add VAT view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- VatNotAddedFormModel.options) {
          assertContainsRadioButton(
            doc,
            option.id,
            "radioOption",
            option.value,
            isChecked = false
          )
        }
      }
    }

    "submitted with invalid data" must {
      "return the form with error messages" in {
        def view(): Html =
          inject[whichAccountAddVat].apply(formWithInvalidData, frontendAppConfig)(
            fakeRequest,
            messages
          )

        val doc = asDocument(view())
        doc.text() must include("Which account do you want to add VAT?")
        doc.text() must include("There is a problem")
        doc.text() must include("Select which account you want to add VAT")
        doc.text() must include("Select an option")
        doc.text() must include("Sign into your other account to add VAT")
        doc.text() must include("Add VAT to this account")
        doc.select("title").text() must include(
          "Error: Which account do you want to add VAT?"
        )
      }
    }
  }

}
