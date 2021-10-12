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

import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json._
import java.time.LocalDateTime

class UserEnrolmentsSpec extends WordSpec with MustMatchers with GuiceOneServerPerSuite {

  val service: String = "HMRC-VATVAR-ORG"
  val activeStatus: String = "active"
  val inactiveStatus: String = "inactive"

  val essentialUserEnrolmentStatus: String =
    s"""{"service":"$service","state":"$inactiveStatus","enrolmentTokenExpiryDate":"2018-05-07 12:00:02.000"}"""

  val extendedUserEnrolmentStatus: String =
    s"""
       |{
       |  "service": "$service",
       |  "state": "$inactiveStatus",
       |  "enrolmentTokenExpiryDate": "2018-05-07 12:00:02.000",
       |  "friendlyName": "Vat Var Enrolment",
       |  "enrolmentDate": "2019-01-31 14:48:00.000",
       |  "failedActivationCount": 0,
       |  "activationDate": "",
       |  "identifiers":
       |         [
       |            {
       |               "key": "VatRegNo",
       |               "value": "555369055"
       |            },
       |            {
       |               "key": "PostCode",
       |               "value": "PJ1 1ZD"
       |            }
       |         ]
       |}
       |""".stripMargin

  val enrolments: String =
    s"""
       |{
       |  "enrolments":
       |  [
       |    {"service":"$service","state":"$inactiveStatus","enrolmentTokenExpiryDate":"2018-05-07 12:00:02.000"},
       |    {"service":"$service","state":"$activeStatus","enrolmentTokenExpiryDate":"2020-06-10 08:08:04.000"}
       |  ]
       |}
       |""".stripMargin

  val expectedEnrolmentTokenExpiryDate1: LocalDateTime = LocalDateTime.parse("2018-05-07T12:00:02.000")
  val expectedEnrolmentTokenExpiryDate2: LocalDateTime = LocalDateTime.parse("2020-06-10T08:08:04.000")

  val expectedEssentialUserEnrolmentStatus: UserEnrolmentStatus = UserEnrolmentStatus(service, Some(inactiveStatus), Some(expectedEnrolmentTokenExpiryDate1))

  val expectedUserEnrolmentStatusList: List[UserEnrolmentStatus] = List(
    UserEnrolmentStatus(service, Some(inactiveStatus), Some(expectedEnrolmentTokenExpiryDate1)),
    UserEnrolmentStatus(service, Some(activeStatus), Some(expectedEnrolmentTokenExpiryDate2))
  )

  val expectedUserEnrolments: UserEnrolments = UserEnrolments(expectedUserEnrolmentStatusList)

  "UserEnrolmentStatus" should {

    "create an instance of UserEnrolmentStatus from a valid Json representation" in {

      val userEnrolmentStatusAsJsValue: JsValue = Json.parse(essentialUserEnrolmentStatus)

      userEnrolmentStatusAsJsValue.validate[UserEnrolmentStatus] match {
        case JsSuccess(actualUserEnrolmentStatus, _) => actualUserEnrolmentStatus mustBe expectedEssentialUserEnrolmentStatus
        case e: JsError => fail(s"Parsing of UserEnrolmentStatus Json representation failed : $e")
      }

    }

    "create an instance of UserEnrolmentStatus from an extensive Json representation" in {

      val userEnrolmentStatusAsJsValue: JsValue = Json.parse(extendedUserEnrolmentStatus)

      userEnrolmentStatusAsJsValue.validate[UserEnrolmentStatus] match {
        case JsSuccess(actualUserEnrolmentStatus, _) => actualUserEnrolmentStatus mustBe expectedEssentialUserEnrolmentStatus
        case e: JsError => fail(s"Parsing of extensive UserEnrolmentStatus Json representation failed : $e")
      }

    }

    "correctly serialize an instance of UserEnrolmentStatus" in {

      val serializedUserEnrolmentStatus: String = Json.toJson(expectedEssentialUserEnrolmentStatus).toString()

      serializedUserEnrolmentStatus mustBe essentialUserEnrolmentStatus

    }

  }

  "UserEnrolments" should {

    "correctly create an instance of UserEnrolments from a valid Json representation" in {

      val userEnrolmentsAsJsValue: JsValue = Json.parse(enrolments)

      userEnrolmentsAsJsValue.validate[UserEnrolments] match {
        case JsSuccess(actualUserEnrolments, _) => actualUserEnrolments mustBe expectedUserEnrolments
        case e: JsError => fail(s"Parsing of user enrolments Json failed : $e")
      }
    }
  }
}
