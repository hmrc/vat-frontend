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

import base.SpecBase
import utils.CurrencyFormatter
class CurrencyFormatterSpec extends SpecBase {

  "CurrencyFormatter.formatCurrency" must {

    "correctly format an amount" which {

      "is 1.12" in {
        CurrencyFormatter.formatCurrency(1.12) mustBe "£1.12"
      }

      "is 1.1" in {
        CurrencyFormatter.formatCurrency(1.1) mustBe "£1.10"
      }

      "is 1.01" in {
        CurrencyFormatter.formatCurrency(1.01) mustBe "£1.01"
      }

      "is 0.12" in {
        CurrencyFormatter.formatCurrency(0.01) mustBe "£0.01"
      }

      "is 10.00" in {
        CurrencyFormatter.formatCurrency(10.00) mustBe "£10"
      }

      "is 1000.12" in {
        CurrencyFormatter.formatCurrency(1000.12) mustBe "£1,000.12"
      }

      "is 1000000" in {
        CurrencyFormatter.formatCurrency(1000000) mustBe "£1,000,000"
      }

      "is 10000000000.99" in {
        CurrencyFormatter.formatCurrency(10000000000.99) mustBe "£10,000,000,000.99"
      }
    }
  }

  "CurrencyFormatter.formatCurrencyFromPennies" must {

    "correctly format an amount" which {

      "is 1.12" in {
        CurrencyFormatter.formatCurrencyFromPennies(112) mustBe "£1.12"
      }

      "is 1.1" in {
        CurrencyFormatter.formatCurrencyFromPennies(110) mustBe "£1.10"
      }

      "is 1.01" in {
        CurrencyFormatter.formatCurrencyFromPennies(101) mustBe "£1.01"
      }

      "is 0.12" in {
        CurrencyFormatter.formatCurrencyFromPennies(1) mustBe "£0.01"
      }

      "is 10.00" in {
        CurrencyFormatter.formatCurrencyFromPennies(1000) mustBe "£10"
      }

      "is 1000.12" in {
        CurrencyFormatter.formatCurrencyFromPennies(100012) mustBe "£1,000.12"
      }

      "is 1000000" in {
        CurrencyFormatter.formatCurrencyFromPennies(100000000) mustBe "£1,000,000"
      }

      "is 10000000000.99" in {
        CurrencyFormatter.formatCurrencyFromPennies(1000000000099L) mustBe "£10,000,000,000.99"
      }
    }
  }
}