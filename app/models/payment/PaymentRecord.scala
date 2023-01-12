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

package models.payment

import models.payment.PaymentRecord._
import play.api.i18n.Messages
import play.api.libs.json._
import play.twirl.api.HtmlFormat
import utils.CurrencyFormatter

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.Locale
import scala.util.{Failure, Success, Try}

case class PaymentRecord(reference: String,
                         amountInPence: Long,
                         createdOn: LocalDateTime,
                         taxType: String) {

  def dateFormatted(implicit messages: Messages): String =
    DateFormatting.formatFull(createdOn.toLocalDate)

  def currencyFormatted: String =
    CurrencyFormatter.formatCurrencyFromPennies(amountInPence)

  def currencyFormattedBold()(implicit messages: Messages): HtmlFormat.Appendable = {
    CurrencyFormatter.formatBoldCurrencyFromPennies(amountInPence)
  }
}

object PaymentRecord {

  val datePattern: String = "d MMMM yyyy"
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH)

  private[payment] object DateFormatting {
    def formatFull(date: LocalDate)(implicit messages: Messages): String = {
      messages.lang.code match {
        case "cy" =>
          val month: String = messages(s"bta.month.${date.getMonthValue}")
          s"${date.getDayOfMonth} $month ${date.getYear}"
        case _ => dateFormatter.format(date)
      }
    }
  }

  def from(paymentRecordData: VatPaymentRecord): Option[PaymentRecord] = {
    if (paymentRecordData.isValid && paymentRecordData.isSuccessful) {
      Some(PaymentRecord(
        reference = paymentRecordData.reference,
        amountInPence = paymentRecordData.amountInPence,
        createdOn = LocalDateTime.parse(paymentRecordData.createdOn),
        taxType = paymentRecordData.taxType
      ))
    } else {
      None
    }
  }

  private def dateTimeReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {

    override def reads(json: JsValue): JsResult[LocalDateTime] = json.validate[String] match {
      case JsSuccess(string, jsPath) => Try(LocalDateTime.parse(string)) match {
        case Success(value) => JsSuccess(value, jsPath)
        case Failure(exception) => {
          JsError("not a valid date " + exception)

        }
      }
      case JsError(err) => JsError(err)
    }
  }

  private def dateTimeWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    override def writes(dateTime: LocalDateTime): JsValue = JsString(dateTime.toString)
  }

  private implicit lazy val dateTimeFormat: Format[LocalDateTime] = Format(dateTimeReads, dateTimeWrites)

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

case class VatPaymentRecord (reference: String,
                            amountInPence: Long,
                            status: PaymentStatus,
                            createdOn: String,
                            taxType: String) {

  def getDateTime: LocalDateTime = {
    OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime
  }

  def isValid: Boolean = {
    val daysToAdd = 7
      Try(LocalDateTime.parse(createdOn).plusDays(daysToAdd).isAfter(getDateTime)).getOrElse(false)
  }

  def isSuccessful: Boolean = {
    status == PaymentStatus.Successful
  }

}

object VatPaymentRecord {
  implicit val format: OFormat[VatPaymentRecord] = Json.format[VatPaymentRecord]
}

case object PaymentRecordFailure
