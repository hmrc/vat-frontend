/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors.models

import play.api.libs.json.{Json, OFormat}


case class AccountSummaryData(accountBalance: Option[AccountBalance],
                              dateOfBalance: Option[String],
                              openPeriods: Seq[OpenPeriod] = Seq.empty
                             ) {
  def isValid: Boolean = accountBalance.exists( _.amount.isDefined )
}

object AccountSummaryData {
  implicit val formats: OFormat[AccountSummaryData] = Json.format[AccountSummaryData]

  def balanceExpression(a: AccountSummaryData, expression: ((BigDecimal) => Boolean)) = a.accountBalance match {
    case Some(accountBalance) => accountBalance.amount match {
      case Some(amount) => expression(amount)
      case _ => false
    }
    case _ => false
  }

  def accountBalanceIsZero(a: AccountSummaryData) = balanceExpression(a, _ == 0)

  def balanceGreaterThanZero(a: AccountSummaryData) = balanceExpression(a, _ > 0)

  def balanceLessThanZero(a: AccountSummaryData) = balanceExpression(a, _ < 0)

  def hasOpenPeriods(a: AccountSummaryData) = a.openPeriods.nonEmpty
}
