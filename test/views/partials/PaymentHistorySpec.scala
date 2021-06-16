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

package views.partials

import models.payment.{PaymentRecord, PaymentRecordFailure}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Messages, MessagesApi}
import views.html.partials.payment_history
import java.time.LocalDateTime
import java.util.UUID
import scala.collection.JavaConverters._
import scala.util.Random

class PaymentHistorySpec extends WordSpec with MustMatchers with GuiceOneServerPerSuite {

  implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  def testReference: String = UUID.randomUUID().toString

  def testAmount: Long = Random.nextLong().abs

  val testCreatedOn: LocalDateTime = LocalDateTime.parse("2018-10-21T08:00:00.000")
  val testTaxType: String = "tax type"

  def newTestPaymentRecord = PaymentRecord(
    reference = testReference,
    amountInPence = testAmount,
    createdOn = testCreatedOn,
    taxType = testTaxType
  )

  def view(maybeHistory: Either[PaymentRecordFailure.type, List[PaymentRecord]]): Document = Jsoup.parse(payment_history(maybeHistory).toString())

  "PaymentHistory" when {
    "maybeHistory is Right and a list of exactly one record" should {
      "display the single record version of the history" in {
        val testPaymentRecord = newTestPaymentRecord
        val doc = view(Right(List(testPaymentRecord)))

        val h2s = doc.getElementsByTag("h2")
        h2s.size() mustBe 1
        h2s.get(0).text mustBe "Your card payments in the last 7 days"

        doc.getElementsByTag("ul").size mustBe 0

        val paragraphs = doc.getElementsByTag("p")
        paragraphs.size mustBe 3

        paragraphs.get(0).attr("id") mustBe "single-payment-history"
        paragraphs.get(0).text() mustBe s"You paid ${testPaymentRecord.currencyFormatted} on ${testPaymentRecord.dateFormatted}"
        paragraphs.get(1).attr("id") mustBe "payment-history-reference"
        paragraphs.get(1).text() mustBe s"Your payment reference number is ${testPaymentRecord.reference}."

        paragraphs.get(2).text mustBe "It will take up to 7 days to update your balance after each payment."
      }
    }
    "maybeHistory is Right and a list of more than one record" should {
      "display the single record version of the history" in {
        val testPaymentRecords = List(newTestPaymentRecord, newTestPaymentRecord)
        val doc = view(Right(testPaymentRecords))

        val h2s = doc.getElementsByTag("h2")
        h2s.size() mustBe 1
        h2s.get(0).text mustBe "Your card payments in the last 7 days"

        doc.getElementsByTag("ul").size mustBe 1
        val listItems = doc.getElementsByTag("li")
        listItems.size mustBe testPaymentRecords.size

        (listItems.asScala zip testPaymentRecords).foreach { case (htmlItem, dataItem) =>
          htmlItem.text() mustBe s"You paid ${dataItem.currencyFormatted} on ${dataItem.dateFormatted}"
        }

        val paragraphs = doc.getElementsByTag("p")
        paragraphs.size mustBe 1
        paragraphs.get(0).text mustBe "It will take up to 7 days to update your balance after each payment."
      }
    }
    "maybeHistory is Right and a list is empty" should {
      "return an empty html" in {
        val doc = view(Right(List.empty))
        doc.text().trim mustBe ""
      }
    }
    "maybeHistory is Left" should {
      "return error message" in {
        val doc = view(Left(PaymentRecordFailure))
        doc.select("p.bold-small").text mustBe "We cannot display your card payment history at the moment. Check again later."
      }
    }
  }

}
