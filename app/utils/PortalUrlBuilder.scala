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

package utils

import models.VatEnrolment
import play.api.i18n.Lang
import play.api.mvc.Request
import uk.gov.hmrc.play.language.LanguageUtils
import uk.gov.hmrc.urls.UrlBuilder
import utils.PortalUrlBuilder._

trait PortalUrlBuilder {

  def languageUtils: LanguageUtils

  def buildPortalUrl(url: String)(enrolment: Option[VatEnrolment])(implicit request: Request[_]): String = {
    val replacedUrl = UrlBuilder.buildUrl(url, Seq(("<vrn>", enrolment.map(_.vrn))))
    appendLanguage(replacedUrl)
  }

  private def appendLanguage(url: String)(implicit request: Request[_]) = {
    val lang = if (languageUtils.getCurrentLang == WELSH) "lang=cym" else "lang=eng"
    val token = if (url.contains("?")) "&" else "?"
    s"$url$token$lang"
  }
}

object PortalUrlBuilder {
  val WELSH: Lang = Lang("cy")
}
