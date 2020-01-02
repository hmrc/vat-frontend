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

package models.payment

import java.util.UUID

import org.joda.time.DateTime
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import play.api.test.FakeRequest

import scala.util.Random

class PaymentRecordSpec extends WordSpec with MustMatchers with GuiceOneServerPerSuite {

  val testReference: String = UUID.randomUUID().toString
  val testAmountInPence: Long = Random.nextLong()
  val currentDateTime: DateTime = DateTime.now()
  val testCreatedOn: String = currentDateTime.toString
  val testTaxType: String = "testTaxType"

  val testPaymentRecord = PaymentRecord(
    reference = testReference,
    amountInPence = testAmountInPence,
    createdOn = currentDateTime,
    taxType = testTaxType
  )

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  def testJson(createOn: String): String =
    s"""
       |{
       |  "reference" : "$testReference",
       |  "amountInPence" : $testAmountInPence,
       |  "status" : ${Json.toJson(PaymentStatus.Successful).toString()},
       |  "createdOn" : "$createOn",
       |  "taxType" : "$testTaxType"
       |}
    """.stripMargin

  "PaymentRecord.from" should {
    "return None" when {
      "the status is not Successful" in {
        val invalidVatPaymentRecordData = VatPaymentRecord(testReference, testAmountInPence, PaymentStatus.Invalid, testCreatedOn, testTaxType)
        PaymentRecord.from(invalidVatPaymentRecordData, currentDateTime) mustBe None
      }
      "the createdOn is an invalid dateTime" in {
        val invalidCreatedOnVatPaymentRecordData = VatPaymentRecord(testReference, testAmountInPence, PaymentStatus.Successful, "", testTaxType)
        PaymentRecord.from(invalidCreatedOnVatPaymentRecordData, currentDateTime) mustBe None
      }
    }
    "return Some(PaymentRecord)" in {
      val validVatPaymentRecordData = VatPaymentRecord(testReference, testAmountInPence, PaymentStatus.Successful, testCreatedOn, testTaxType)
      val expected = Some(PaymentRecord(
        reference = testReference,
        amountInPence = testAmountInPence,
        createdOn = currentDateTime,
        taxType = testTaxType
      ))
      PaymentRecord.from(validVatPaymentRecordData, currentDateTime) mustBe expected
    }
  }

  "format" should {
    "parse the json correctly if the createOn is a valid DateTime" in {
      val expected: PaymentRecord = testPaymentRecord
      Json.fromJson[PaymentRecord](Json.parse(testJson(testCreatedOn))) mustBe JsSuccess(expected)
    }
    "fail to parse if the createOn is an invalid DateTime" in {
      Json.fromJson[PaymentRecord](Json.parse(testJson(""))) mustBe an[JsError]
    }

    "output of the writer should be readable by its own reader" in {
      val init: PaymentRecord = testPaymentRecord
      val writtenJson = Json.toJson(init)
      Json.fromJson[PaymentRecord](writtenJson) mustBe JsSuccess(init)
    }
  }

  "eitherPaymentHistoryFormatter" should {
    type TestE = Either[PaymentRecordFailure.type, List[PaymentRecord]]

    def testJsonString(paymentHistory: TestE): JsValue =
      paymentHistory.right.map(Json.toJson[List[PaymentRecord]](_)).left.map(_ => JsString("Bad Gateway")).merge

    "parse JsArray of PaymentRecord as right" in {
      val altTestReference = "anotherReference"
      val json: JsValue = testJsonString(Right(List(testPaymentRecord, testPaymentRecord.copy(reference = altTestReference))))
      Json.fromJson[TestE](json).get mustBe Right(List(testPaymentRecord, testPaymentRecord.copy(reference = altTestReference)))
    }
    "parse JsArray of any other type(s) asLeft" in {
      val json: JsValue = Json.parse("""[ "string", 1 ]""")
      Json.fromJson[TestE](json).get mustBe Left(PaymentRecordFailure)
    }
    "parse anything else as Left" in {
      val json: JsValue = testJsonString(Left(PaymentRecordFailure))
      Json.fromJson[TestE](json).get mustBe Left(PaymentRecordFailure)
    }
    "for Right(list) output JsArray" in {
      val testCard: TestE = Right(Nil)
      Json.toJson(testCard) mustBe JsArray()
    }
    "for Left(PaymentRecordFailure) output the JsString of Bad Gateway" in {
      val testCard: TestE = Left(PaymentRecordFailure)
      Json.toJson(testCard) mustBe JsString(PaymentRecord.paymentRecordFailureString)
    }
  }

  "PaymentRecord.dateFormatted" should {
    "display the date in d MMMM yyyy format" in {
      val testDate: DateTime = currentDateTime
      val testRecord = testPaymentRecord.copy(createdOn = testDate)
      testRecord.dateFormatted mustBe s"${testDate.dayOfMonth().get()} ${testDate.monthOfYear().getAsText} ${testDate.year().get()}"
    }
  }

  "PaymentRecord.currencyFormatted" should {
    "for whole pounds under 1000 add the £ and no decimals" in {
      val testAmount = 99900
      val testRecord = testPaymentRecord.copy(amountInPence = testAmount)
      testRecord.currencyFormatted mustBe "£999"
    }
    "for whole pounds over 1000 add ," in {
      val testAmount = 100000000
      val testRecord = testPaymentRecord.copy(amountInPence = testAmount)
      testRecord.currencyFormatted mustBe "£1,000,000"
    }
    "for any none zero pence values show 2 decimal places" in {
      val testAmount = 100000010
      val testRecord = testPaymentRecord.copy(amountInPence = testAmount)
      testRecord.currencyFormatted mustBe "£1,000,000.10"
    }
  }

}
