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

package views

import forms.StopFormProvider
import models.Stop
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.stop

class StopViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "stop"

  val form = new StopFormProvider()()

  val serviceInfoContent = HtmlFormat.empty

  def createView = () => stop(frontendAppConfig, form)(serviceInfoContent)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => stop(frontendAppConfig, form)(serviceInfoContent)(fakeRequest, messages)

  "Stop view" must {
    behave like normalPage(createView, messageKeyPrefix, "hint.text")
  }

  "Stop view" when {
    "rendered" must {

      "contain a 'I don’t want to do this right now' link" in {
        val doc = asDocument(createViewUsingForm(form))
        val notNowLink = doc.getElementById("not-now")
        notNowLink.text mustBe "I don’t want to do this right now"
        notNowLink.attr("href") mustBe "http://localhost:9020/business-account"
        notNowLink.attr("data-journey-click") mustBe "CtStop:Click:NotNow"
      }

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- Stop.options) {
          assertContainsRadioButton(doc, option.id, "value", option.value, false)
        }
      }
    }

    for (option <- Stop.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, option.id, "value", option.value, true)

          for (unselectedOption <- Stop.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
