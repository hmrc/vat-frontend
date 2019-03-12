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

package models.payment

import org.joda.time.{DateTime, LocalDate}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Reads, Writes}
import util.CurrencyFormatter
import play.api.libs.functional.syntax._

case class PaymentRecord(
                          reference: String,
                          amountInPence: Long,
                          status: PaymentStatus,
                          createdOn: String,
                          taxType: String) {

  def isValid(currentDateTime: LocalDate): Boolean = {

    try {
      val date = new DateTime(createdOn)
      if(date.plusDays(7).isAfter(currentDateTime.toDateTimeAtCurrentTime)) {
        true
      }
      else false
    } catch {
      case _: IllegalArgumentException => false
    }
  }

  val isSuccessful: Boolean = {
    status == Successful
  }

  def dateFormatted()(implicit messages: Messages) = {
    try {
      DateFormatting.formatFull(new DateTime(createdOn).toLocalDate)
    } catch {
      case _: IllegalArgumentException => "problem displaying date"
    }
  }

  def currencyFormatted()(implicit messages: Messages) = {
    CurrencyFormatter.formatCurrencyFromPennies(amountInPence)
  }

  object DateFormatting {

    import com.ibm.icu.text.SimpleDateFormat
    import com.ibm.icu.util.{TimeZone, ULocale}

    def formatFull(date: LocalDate)(implicit messages: Messages): String =
      createDateFormatForPattern("d MMMM yyyy", messages).format(date.toDate)

    private def createDateFormatForPattern(pattern: String, messages: Messages): SimpleDateFormat = {
      val langCode = messages.lang.code
      val uk = TimeZone.getTimeZone("Europe/London")
      val validLang: Boolean = ULocale.getAvailableLocales.contains(new ULocale(langCode))
      val locale: ULocale = if (validLang) new ULocale(langCode) else ULocale.getDefault
      val sdf = new SimpleDateFormat(pattern, locale)
      sdf.setTimeZone(uk)
      sdf
    }

  }

}

object PaymentRecord {

  implicit val writes: Writes[PaymentRecord] = (
    (JsPath \ "reference").write[String] and
      (JsPath \ "amountInPence").write[Long] and
      (JsPath \ "status").write[PaymentStatus](PaymentStatus.paymentStatusWrites) and
      (JsPath \ "createdOn").write[String] and
      (JsPath \ "taxType").write[String]
    )(unlift(PaymentRecord.unapply))

  implicit val reads: Reads[PaymentRecord] = (
    (JsPath \ "reference").read[String] and
      (JsPath \ "amountInPence").read[Long] and
      (JsPath \ "status").read[PaymentStatus](PaymentStatus.paymentStatusReads) and
      (JsPath \ "createdOn").read[String] and
      (JsPath \ "taxType").read[String]
    )(PaymentRecord.apply _)
}
