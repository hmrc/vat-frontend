/*
 * Copyright 2020 HM Revenue & Customs
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

package config.portal

import javax.inject.Inject
import models.VatEnrolment
import play.api.i18n.MessagesApi
import play.api.mvc.Request

class PortalUrlConfig @Inject()(implicit private val messagesApi: MessagesApi) {

  def changeRepaymentsAccount(vat: VatEnrolment)(implicit request: Request[_]): String = appendLanguage(s"/vat-variations/org/${vat.vrn}/introduction")

  private def appendLanguage(url: String)(implicit request: Request[_]): String = {
    val portalLang = Map(
      "cy" -> "cym",
      "en" -> "eng"
    ).get(messagesApi.preferred(request).lang.language)

    val urlWithLang = portalLang.map { portalLang =>
      val paramChar = if (url.contains("?")) "&" else "?"
      s"$url${paramChar}lang=$portalLang"
    }

    urlWithLang getOrElse url
  }

}
