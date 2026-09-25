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

package views

import models.requests.ListLinks
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import views.behaviours.ViewBehaviours
import views.html.service_info

class ServiceInfoViewSpec extends ViewBehaviours {

  lazy val view: service_info = app.injector.instanceOf[service_info]

  override implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  "BtaNavigation view" should {
    "filter out menu items where showBoolean is false" in {
      val navLinks = Seq(
        ListLinks(
          message = "Home",
          url = "/business-account",
          showBoolean = Some(true),
          alerts = None),
        ListLinks(
            message = "Hidden",
            url = "/hidden",
            showBoolean = Some(false),
            alerts = None
        )
      )
      val html = view(navLinks, "home").body
      html must include("Home")
      html must not include ("Hidden")
    }

    "mark the active tab correctly" in {
      val navLinks = Seq(
          ListLinks(
            message = "Messages",
            url = "/business-account/messages",
            showBoolean = Some(true),
            alerts = None
      )
      )
      val html = view(navLinks, "messages").body
      val doc = Jsoup.parse(html)
      doc.select(".govuk-service-navigation__item--active").size() mustBe 1
    }

    "handle null navLinks safely" in {
      noException should be thrownBy {
        view(null, "home").body
      }
    }
  }


}
