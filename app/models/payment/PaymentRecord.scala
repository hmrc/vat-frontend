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

import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.{TimeZone, ULocale}
import models.payment.PaymentRecord._
import org.joda.time.{DateTime, DateTimeConstants, LocalDate}
import play.api.i18n.Messages
import play.api.libs.json._
import utils.CurrencyFormatter

import scala.util.{Failure, Success, Try}

case class PaymentRecord(reference: String,
                         amountInPence: Long,
                         createdOn: DateTime,
                         taxType: String) {

  def dateFormatted(implicit messages: Messages): String =
    DateFormatting.formatFull(createdOn.toLocalDate)

  def currencyFormatted: String =
    CurrencyFormatter.formatCurrencyFromPennies(amountInPence)

}

object PaymentRecord {

  private[payment] object DateFormatting {
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

  def from(paymentRecordData: VatPaymentRecord, currentDateTime: DateTime): Option[PaymentRecord] =
    if (paymentRecordData.isValid(currentDateTime) && paymentRecordData.isSuccessful) {
      Some(PaymentRecord(
        reference = paymentRecordData.reference,
        amountInPence = paymentRecordData.amountInPence,
        createdOn = new DateTime(paymentRecordData.createdOn),
        taxType = paymentRecordData.taxType
      ))
    } else {
      None
    }

  private def dateTimeReads: Reads[DateTime] = new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = json.validate[String] match {
      case JsSuccess(string, jsPath) => Try(new DateTime(string)) match {
        case Success(value) => JsSuccess(value, jsPath)
        case Failure(exception) => JsError("not a valid date " + exception)
      }
      case JsError(err) => JsError(err)
    }
  }

  private def dateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = JsString(dateTime.toString)
  }

  private implicit lazy val dateTimeFormat: Format[DateTime] = Format(dateTimeReads, dateTimeWrites)

  implicit lazy val format: OFormat[PaymentRecord] = Json.format[PaymentRecord]

  private[models] val paymentRecordFailureString = "Bad Gateway"

  private def eitherPaymentHistoryReader: Reads[Either[PaymentRecordFailure.type, List[PaymentRecord]]] =
    new Reads[Either[PaymentRecordFailure.type, List[PaymentRecord]]] {
      override def reads(json: JsValue): JsResult[Either[PaymentRecordFailure.type, List[PaymentRecord]]] =
        json.validate[List[PaymentRecord]] match {
          case JsSuccess(validList, jsPath) => JsSuccess(Right(validList), jsPath)
          case _ => JsSuccess(Left(PaymentRecordFailure))
        }
    }

  private def eitherPaymentHistoryWriter: Writes[Either[PaymentRecordFailure.type, List[PaymentRecord]]] =
    new Writes[Either[PaymentRecordFailure.type, List[PaymentRecord]]] {
      override def writes(eitherPaymentHistory: Either[PaymentRecordFailure.type, List[PaymentRecord]]): JsValue = eitherPaymentHistory match {
        case Right(list) => Json.toJson(list)
        case _ => JsString(paymentRecordFailureString)
      }
    }

  implicit lazy val eitherPaymentHistoryFormatter: Format[Either[PaymentRecordFailure.type, List[PaymentRecord]]] =
    Format(eitherPaymentHistoryReader, eitherPaymentHistoryWriter)

}

case class VatPaymentRecord(reference: String,
                            amountInPence: Long,
                            status: PaymentStatus,
                            createdOn: String,
                            taxType: String) {

  def isValid(currentDateTime: DateTime): Boolean =
    Try(new DateTime(createdOn).plusDays(DateTimeConstants.DAYS_PER_WEEK).isAfter(currentDateTime)).getOrElse(false)

  def isSuccessful: Boolean = status == PaymentStatus.Successful

}

object VatPaymentRecord {
  implicit val format: OFormat[VatPaymentRecord] = Json.format[VatPaymentRecord]
}

case object PaymentRecordFailure
