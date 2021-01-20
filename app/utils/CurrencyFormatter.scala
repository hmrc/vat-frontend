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

package utils

object CurrencyFormatter {
  def formatCurrency(amount: BigDecimal, symbol:String = "£"): String = {

    val absoluteAmount = amount.abs
    val pence = (absoluteAmount * 100).toBigInt()
    val pounds = absoluteAmount.toBigInt()

    val negative = if (amount < 0) {
      "-"
    } else {
      ""
    }

    val formattedAmount = if(pounds * 100 == pence){
      "%,d".format(pounds)
    } else {
      "%,.2f".format(absoluteAmount)
    }
    s"$negative$symbol$formattedAmount"
  }

  def formatCurrencyFromPennies(amount: Long, symbol:String = "£"): String = {

    val pounds = BigDecimal(amount) / 100

    formatCurrency(pounds, symbol)
  }

}
