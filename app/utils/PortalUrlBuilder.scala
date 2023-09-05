/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.Logger
import play.api.i18n.Lang
import play.api.mvc.Request
import uk.gov.hmrc.play.language.LanguageUtils
import utils.PortalUrlBuilder._

import scala.annotation.tailrec

trait UrlBuilder {

  val logger = Logger("application")

  def buildUrl(destinationUrl: String, tags: Seq[(String, Option[Any])]): String = {
    resolvePlaceHolder(destinationUrl, tags)
  }

  @tailrec
  private def resolvePlaceHolder(url: String, tags: Seq[(String, Option[Any])]): String =
    if (tags.isEmpty) url
    else resolvePlaceHolder(replace(url, tags.head), tags.tail)

  private def replace(url: String, tags: (String, Option[Any])): String = {
    val (tagName, tagValueOption) = tags
    tagValueOption match {
      case Some(valueOfTag) => url.replace(tagName, valueOfTag.toString)
      case _ =>
        if (url.contains(tagName)) {
          logger.error(s"Failed to populate parameter $tagName in URL $url")
        }
        url
    }
  }
}

trait PortalUrlBuilder extends UrlBuilder {

  def languageUtils: LanguageUtils

  def buildPortalUrl(url: String)(enrolment: Option[VatEnrolment])(implicit request: Request[_]): String = {
    val replacedUrl = buildUrl(url, Seq(("<vrn>", enrolment.map(_.vrn))))
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
