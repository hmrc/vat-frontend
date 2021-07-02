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

import base.SpecBase
import java.time.{LocalDate, LocalDateTime}

class DirectDebitStatusSpec extends SpecBase {
  "The DirectDebitStatus.from method" when {

    val activeDirectDebitDetails = DirectDebitActive(LocalDate.now(), LocalDate.now().plusDays(1))

    "the eligibility flag is true and active direct debit information exists" should {
      "return an ActiveDirectDebit entity with the relevant details" in {
        val result = DirectDebitStatus.from(DirectDebit(ddiEligibilityInd = true, active = Some(activeDirectDebitDetails)))
        result mustBe ActiveDirectDebit(activeDirectDebitDetails)
      }
    }
    "the eligibility flag is true and active direct debit information does not exist" should {
      "return an InactiveDirectDebit entity" in {
        val result = DirectDebitStatus.from(DirectDebit(ddiEligibilityInd = true, active = None))
        result mustBe InactiveDirectDebit
      }
    }
    "the eligibility flag is false and active direct debit information exists" should {
      "return an IneligibleDirectDebit entity" in {
        val result = DirectDebitStatus.from(DirectDebit(ddiEligibilityInd = false, active = Some(activeDirectDebitDetails)))
        result mustBe DirectDebitIneligible
      }
    }
    "the eligibility flag is false and active direct debit information does not exist" should {
      "return an IneligibleDirectDebit entity" in {
        val result = DirectDebitStatus.from(DirectDebit(ddiEligibilityInd = false, active = None))
        result mustBe DirectDebitIneligible
      }
    }
  }

}
