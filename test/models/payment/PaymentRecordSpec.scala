/*
 * Copyright 2024 HM Revenue & Customs
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


import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json._
import play.api.test.FakeRequest

import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.{Locale, UUID}
import scala.util.Random
import PaymentStatus._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PaymentRecordSpec extends AnyWordSpec with Matchers with GuiceOneServerPerSuite {

  val testReference: String = UUID.randomUUID().toString
  val testAmountInPence: Long = Random.nextLong()

  val currentDateTime: LocalDateTime = LocalDateTime.now()
  val testCreatedOn: String = currentDateTime.toString
  val testLocalDateTime: LocalDateTime = LocalDateTime.parse(testCreatedOn)
  val testCreatedOnInvalid: String = currentDateTime.plusDays(3).toString
  val testTaxType: String = "testTaxType"

  val testPaymentRecord: PaymentRecord = PaymentRecord(
    reference = testReference,
    amountInPence = testAmountInPence,
    createdOn = testLocalDateTime,
    taxType = testTaxType
  )

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  def testJson(createOn: String): String =
    s"""
       |{
       |  "reference" : "$testReference",
       |  "amountInPence" : $testAmountInPence,
       |  "status" : ${Json.toJson(PaymentStatus.Successful: PaymentStatus).toString()},
       |  "createdOn" : "$createOn",
       |  "taxType" : "$testTaxType"
       |}
    """.stripMargin

  def testJsonPaymentRecord(createOn: String): String =
    s"""
       |{
       |  "reference" : "$testReference",
       |  "amountInPence" : $testAmountInPence,
       |  "createdOn" : "$createOn",
       |  "taxType" : "$testTaxType"
       |}
    """.stripMargin

  val vatReference: String = "9999516240917"
  val vatAmountInPence: Long = 4100
  val vatPaymentStatus: String = "successful"
  val vatCreatedOn: String = "2019-04-21T16:11:04.417"
  val vatTaxType: String = "vat"

  val stubVatPayment: String = s"""{
                                  |  "id": "5c8a6f08700000ea001df59f",
                                  |  "reference": "$vatReference",
                                  |  "transactionReference": "91fda1f6-88d0-4938-9b61-0af4c7c58c53",
                                  |  "amountInPence": $vatAmountInPence,
                                  |  "status": "$vatPaymentStatus",
                                  |  "createdOn": "$vatCreatedOn",
                                  |  "taxType": "$vatTaxType"
                                  |}""".stripMargin

  val expectedVatPayment: VatPaymentRecord = VatPaymentRecord(
    vatReference,
    vatAmountInPence,
    PaymentStatus.Successful: PaymentStatus,
    vatCreatedOn,
    vatTaxType
  )

  val five: Long = 5
  val nine: Long = 9

  "PaymentRecord.from" should {
    "return None" when {
      "the status is not Successful" in {
        val invalidVatPaymentRecordData = VatPaymentRecord(testReference, testAmountInPence, PaymentStatus.Invalid, testCreatedOn, testTaxType)
        PaymentRecord.from(invalidVatPaymentRecordData) mustBe None
      }

      "the createdOn is an invalid dateTime" in {
        val invalidCreatedOnVatPaymentRecordData = VatPaymentRecord(testReference, testAmountInPence, PaymentStatus.Successful, "", testTaxType)
        PaymentRecord.from(invalidCreatedOnVatPaymentRecordData) mustBe None
      }
    }

    "return Some(PaymentRecord)" in {
      val validVatPaymentRecordData = VatPaymentRecord(testReference, testAmountInPence, PaymentStatus.Successful, testCreatedOn, testTaxType)
      val expected = Some(PaymentRecord(
        reference = testReference,
        amountInPence = testAmountInPence,
        createdOn = testLocalDateTime,
        taxType = testTaxType
      ))
      PaymentRecord.from(validVatPaymentRecordData) mustBe expected
    }

  }

  "format" should {
    "parse the json correctly if the createOn is a valid date and time" in {
      val expected: PaymentRecord = testPaymentRecord
      Json.fromJson[PaymentRecord](Json.parse(testJsonPaymentRecord(testLocalDateTime.toString))) mustBe JsSuccess(expected)
    }

    "fail to parse if the createOn is an invalid DateTime" in {
      Json.fromJson[PaymentRecord](Json.parse(testJson("11"))) mustBe an[JsError]
    }

    "output of the writer should be readable by its own reader" in {
      val init: PaymentRecord = testPaymentRecord
      val writtenJson = Json.toJson(init)
      Json.fromJson[PaymentRecord](writtenJson) mustBe JsSuccess(init)
    }

    "create an instance of VatPaymentRecord from a valid Json representation" in {

      val vatPaymentRecordJsValue: JsValue = Json.parse(stubVatPayment)

      vatPaymentRecordJsValue.validate[VatPaymentRecord] match {
        case JsSuccess(vatPaymentRecord, _) => vatPaymentRecord mustBe expectedVatPayment
        case e: JsError => fail(s"Unable to parse vat payment record. Error : $e")
      }
    }

    "correctly serialize an instance of VatPaymentRecord" in {

      val serializedVatPayment: String = Json.toJson(expectedVatPayment).toString()

      val expectedSerializedVatPayment: String =
        s"""{
           |"reference":"9999516240917",
           |"amountInPence":4100,
           |"status":"successful",
           |"createdOn":"2019-04-21T16:11:04.417",
           |"taxType":"vat"
           |}""".stripMargin

      serializedVatPayment mustBe  expectedSerializedVatPayment.replaceAll("\n", "")
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
      val testDate: LocalDateTime = currentDateTime.plusDays(2)
      val testRecord = testPaymentRecord.copy(createdOn = testDate)
      testRecord.dateFormatted mustBe s"${testDate.getDayOfMonth} ${testDate.getMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${testDate.getYear}"
    }

    "display the date in d MMM yyyy format in Welsh" in {

      val welshMessages: Messages = messagesApi.preferred(Seq(Lang("cy")))

      val janTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2020-01-02T00:00:00.000"))
      janTestRecord.dateFormatted(welshMessages) mustBe "2 Ionawr 2020"

      val febTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2020-02-05T00:00:00.000"))
      febTestRecord.dateFormatted(welshMessages) mustBe "5 Chwefror 2020"

      val marTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2020-03-04T00:00:00.000"))
      marTestRecord.dateFormatted(welshMessages) mustBe "4 Mawrth 2020"

      val aprTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2008-04-28T00:00:00.000"))
      aprTestRecord.dateFormatted(welshMessages) mustBe "28 Ebrill 2008"

      val mayTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2017-05-30T00:00:00.000"))
      mayTestRecord.dateFormatted(welshMessages) mustBe "30 Mai 2017"

      val junTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2018-06-03T00:00:00.000"))
      junTestRecord.dateFormatted(welshMessages) mustBe "3 Mehefin 2018"

      val julTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2021-07-07T00:00:00.000"))
      julTestRecord.dateFormatted(welshMessages) mustBe "7 Gorffennaf 2021"

      val augTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2021-08-22T00:00:00.000"))
      augTestRecord.dateFormatted(welshMessages) mustBe "22 Awst 2021"

      val sepTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2015-09-29T00:00:00.000"))
      sepTestRecord.dateFormatted(welshMessages) mustBe "29 Medi 2015"

      val octTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("2020-10-10T00:00:00.000"))
      octTestRecord.dateFormatted(welshMessages) mustBe "10 Hydref 2020"

      val novTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("1998-11-10T00:00:00.000"))
      novTestRecord.dateFormatted(welshMessages) mustBe "10 Tachwedd 1998"

      val decTestRecord = testPaymentRecord.copy(createdOn = LocalDateTime.parse("1999-12-31T00:00:00.000"))
      decTestRecord.dateFormatted(welshMessages) mustBe "31 Rhagfyr 1999"
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

  "VatPaymentRecord" should {

    "identify a payment more than 7 days old as being invalid" in {

      val nineDaysAgo: LocalDateTime = LocalDateTime.now().minusDays(nine)

      val vatPaymentRecord: VatPaymentRecord = expectedVatPayment.copy(createdOn = nineDaysAgo.toString)

      vatPaymentRecord.isValid mustBe false
    }

    "identify a payment less than 7 days old as being valid" in {
      val offset = 5
      val fiveDaysAgo = LocalDateTime.now().plusDays(offset)

      val vatPaymentRecord: VatPaymentRecord = new VatPaymentRecord(
        vatReference,
        vatAmountInPence,
        PaymentStatus.Successful,
        fiveDaysAgo.toString,
        vatTaxType){
        override def getDateTime: LocalDateTime = LocalDateTime.now()
      }

      vatPaymentRecord.isValid mustBe true
    }

    "identify a payment with an invalid creation date as being invalid" in {

      val vatPaymentRecord: VatPaymentRecord = expectedVatPayment.copy(createdOn = "2012-08-40")

      vatPaymentRecord.isValid mustBe false
    }
  }

}
