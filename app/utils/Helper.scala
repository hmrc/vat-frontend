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

package utils

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTimeZone, LocalDate}
import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils


object Helper {

  private def formatter(pattern: String): DateTimeFormatter = {
    val uk = DateTimeZone.forID("Europe/London")
    DateTimeFormat.forPattern(pattern).withZone(uk)
  }

  def formatLocalDate(date: LocalDate)(implicit messages: Messages) =
    LanguageUtils.Dates.formatDate(date)(messages)
}
