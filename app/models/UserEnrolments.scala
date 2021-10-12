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

package models


import play.api.libs.json._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

case class UserEnrolments(enrolments: List[UserEnrolmentStatus])

object UserEnrolments {

  implicit val formats: OFormat[UserEnrolments] = Json.format[UserEnrolments]

}

case class UserEnrolmentStatus(service: String, state: Option[String], enrolmentTokenExpiryDate: Option[LocalDateTime])

object UserEnrolmentStatus {

  private val dateFormat: String = "yyyy-MM-dd HH:mm:ss.SSS"
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

  implicit def enrolmentTokenExpiryDateWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(localDateTime: LocalDateTime): JsValue = JsString(localDateTime.format(formatter))
  }

  implicit def enrolmentTokenExpiryDateReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] =
      Try(JsSuccess(LocalDateTime.parse(json.as[String], formatter), JsPath)).getOrElse(JsError())
  }

  implicit val format: OFormat[UserEnrolmentStatus] = Json.format[UserEnrolmentStatus]

}
