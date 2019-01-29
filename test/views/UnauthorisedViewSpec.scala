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

package views

import forms.VatNotAddedForm
import models.VatNotAddedFormModel
import org.jsoup.nodes.Document
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.unauthorised

class UnauthorisedViewSpec extends ViewBehaviours with MockitoSugar {

  val messageKeyPrefix = "unauthorised"
  val vatNotAddedForm: VatNotAddedForm = injector.instanceOf[VatNotAddedForm]
  val form: Form[VatNotAddedFormModel] = vatNotAddedForm.form

  val validData: Map[String, String] = Map(
    "value" -> VatNotAddedFormModel.options.head.value
  )

  def createViewUsingForm: Form[VatNotAddedFormModel] => Html =
    (form: Form[VatNotAddedFormModel]) => unauthorised(form, frontendAppConfig)(fakeRequest, messages)

  def view = () => unauthorised(form, frontendAppConfig)(fakeRequest, messages)

  "Unauthorised view" must {

    behave like normalPage(view, "unauthorised")

    "have the correct content" in {
      val doc = asDocument(view())
      doc.text() must include ("Your VAT has not been added to this account")
      doc.text() must include ("Sign into your other account to add VAT")
      doc.text() must include ("Add your VAT to this account")
    }

  }

  "Unauthorised view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- VatNotAddedFormModel.options) {
          assertContainsRadioButton(doc, option.id, "radioOption", option.value, false)
        }
      }
    }
  }

}
