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

package models



import org.joda.time.{DateTime, LocalDateTime}
import play.api.libs.json._
import play.api.libs.functional.syntax._


case class UserEnrolments(enrolments: List[UserEnrolmentStatus])

object UserEnrolments {

  implicit val formats: OFormat[UserEnrolments] = Json.format[UserEnrolments]

}

case class UserEnrolmentStatus(service: String, state: Option[String], enrolmentTokenExpiryDate: Option[LocalDateTime])

object UserEnrolmentStatus {

  def enrolmentTokenExpiryDateWrites: Writes[Option[LocalDateTime]] = new Writes[Option[LocalDateTime]] {

    def writes(o: Option[LocalDateTime]): JsValue = JsString(o.toString)

  }

  def enrolmentTokenExpiryDateReads: Reads[Option[LocalDateTime]] = new Reads[Option[LocalDateTime]] {

    override def reads(json: JsValue): JsResult[Option[LocalDateTime]] = {

      json match {

        case JsString(x) => {

          x.toLowerCase match {

            case x if x.nonEmpty => {

              try {

                val date = new DateTime(x).toLocalDateTime

                JsSuccess(Some(date))

              } catch {
                case _:Throwable => JsSuccess(None)
              }
            }
            case _ => JsSuccess(None)
          }
        }
        case _ => JsError()
      }

    }

  }

  implicit val reads: Reads[UserEnrolmentStatus] = (
    (JsPath \ "service").read[String] and
      (JsPath \ "state").readNullable[String] and
      (JsPath \ "enrolmentTokenExpiryDate").read[Option[LocalDateTime]](UserEnrolmentStatus.enrolmentTokenExpiryDateReads)
    )(UserEnrolmentStatus.apply _)

  implicit val writes: Writes[UserEnrolmentStatus] = (
    (JsPath \ "service").write[String] and
      (JsPath \ "state").writeNullable[String] and
      (JsPath \ "enrolmentTokenExpiryDate").write[Option[LocalDateTime]](UserEnrolmentStatus.enrolmentTokenExpiryDateWrites)
    )(unlift(UserEnrolmentStatus.unapply))

}