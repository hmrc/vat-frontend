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

package services

import config.FrontendAppConfig
import connectors.models._
import models._
import models.requests.AuthenticatedRequest
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Vrn
import views.ViewSpecBase
import org.mockito.Mockito.when

class VatPartialBuilderSpec extends ViewSpecBase with OneAppPerSuite with MockitoSugar with MustMatchers {

  trait LocalSetup {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(FakeRequest())

    lazy val vrn: Vrn = Vrn("1234567890")
    lazy val config: FrontendAppConfig = mock[FrontendAppConfig]
    lazy val vatDecEnrolment = VatDecEnrolment(vrn, isActivated = true)
    lazy val vatVarEnrolment = VatVarEnrolment(vrn, isActivated = true)

    lazy val accountBalance = AccountBalance(Some(BigDecimal(0.00)))
    lazy val accountSummaryData: AccountSummaryData = AccountSummaryData(
      accountBalance = Some(accountBalance),
      dateOfBalance = None,
      openPeriods = Seq.empty
    )

    lazy val calendar = Calendar(
      filingFrequency = Monthly,
      directDebit = InactiveDirectDebit
    )

    lazy val openPeriods: Seq[OpenPeriod] = Seq(
      OpenPeriod(new LocalDate(2016, 6, 30)),
      OpenPeriod(new LocalDate(2016, 5, 30))
    )

    def requestWithEnrolment(vatDecEnrolment: VatDecEnrolment, vatVarEnrolment: VatEnrolment): AuthenticatedRequest[AnyContent] = {
      AuthenticatedRequest[AnyContent](FakeRequest(), "", vatDecEnrolment, vatVarEnrolment)
    }
    val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(vatDecEnrolment, vatVarEnrolment)

    val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
    val vatCalendar: Option[Calendar] = Some(Calendar( filingFrequency = Monthly, directDebit = InactiveDirectDebit))
    val vatData: VatAccountData = VatData(vatAccountSummary, vatCalendar)

    when(config.btaManageAccount).thenReturn("http://localhost:9020/business-account/manage-account")
    when(config.getUrl("makeAPayment")).thenReturn("http://localhost:9050/pay-online/vat/make-a-payment?mode=bta")
    when(config.getPortalUrl("vatOnlineAccount")(Some(vatDecEnrolment))(fakeRequestWithEnrolments)).thenReturn(s"http://localhost:8080/portal/vat/trader/$vrn/directdebit?lang=eng")
    when(config.getPortalUrl("vatPaymentsAndRepayments")(Some(vatDecEnrolment))(fakeRequestWithEnrolments)).thenReturn(s"http://localhost:8080/portal/vat/trader/$vrn/account/overview?lang=eng")
  }


  "VatPartialBuilder" should {

    "handle payments" when {

      "the user is in credit with nothing to pay" in new LocalSetup {
        override lazy val accountBalance = AccountBalance(Some(BigDecimal(-12.34)))
        override val vatData = VatData(accountSummaryData.copy(openPeriods = openPeriods), Some(calendar))

        val view: String = new VatPartialBuilderImpl(config).buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages).body
        val doc = Jsoup.parse(view)

        doc.text().contains("You are £12.34 in credit.") mustBe true
        doc.text().contains("If you have set up a repayments bank account, we will transfer you this money. If not, we will send you a payable order by post. This usually takes less than 10 days but can take up to 20 days.") mustBe true

        assertLinkById(
          doc,
          linkId = "vat-repayments-account",
          expectedText = "repayments bank account",
          expectedUrl = "http://localhost:9020/business-account/manage-account#bank",
          expectedGAEvent = "link - click:VATaccountSummary:repayments bank account",
          expectedIsExternal = false,
          expectedOpensInNewTab = false
        )
      }

      "the user is in debit and has no Direct Debit set up" in new LocalSetup {
        override lazy val accountBalance = AccountBalance(Some(BigDecimal(12.34)))
        override val vatData = VatData(accountSummaryData.copy(openPeriods = openPeriods), Some(calendar))

        val view: String = new VatPartialBuilderImpl(config).buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages).body
        val doc = Jsoup.parse(view)

        doc.text().contains("You owe £12.34") mustBe true
        doc.text().contains("Make a VAT payment or set up a VAT Direct Debit") mustBe true

        assertLinkById(
          doc,
          linkId = "vat-make-payment-link",
          expectedText = "Make a VAT payment",
          expectedUrl = "http://localhost:9050/pay-online/vat/make-a-payment?mode=bta",
          expectedGAEvent = "link - click:VAT cards:Make a VAT payment",
          expectedIsExternal = false,
          expectedOpensInNewTab = false
        )

        assertLinkById(
          doc,
          linkId = "vat-direct-debit-setup-link",
          expectedText = "set up a VAT Direct Debit",
          expectedUrl = s"http://localhost:8080/portal/vat/trader/$vrn/directdebit?lang=eng",
          expectedGAEvent = "link - click:VAT cards:Set up a VAT Direct Debit",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "the user has no tax to pay" in new LocalSetup {
        override val vatData = VatData(accountSummaryData.copy(openPeriods = openPeriods), Some(calendar))

        val view: String = new VatPartialBuilderImpl(config).buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages).body
        val doc = Jsoup.parse(view)

        doc.text().contains("You have no tax to pay.") mustBe true

        assertLinkById(
          doc,
          linkId = "vat-view-statement-link",
          expectedText = "View your VAT statement",
          expectedUrl = s"http://localhost:8080/portal/vat/trader/$vrn/account/overview?lang=eng",
          expectedGAEvent = "link - click:VAT cards:View your vat statement",
          expectedIsExternal = true,
          expectedOpensInNewTab = true
        )
      }

      "the use has no balance to show (user new to service with no submitted returns)" in new LocalSetup {
        override val vatData = VatNoData

        val view: String = new VatPartialBuilderImpl(config).buildPaymentsPartial(vatData)(fakeRequestWithEnrolments, messages).body
        val doc = Jsoup.parse(view)

        doc.text().contains("There is no balance information to display.") mustBe true
      }

    }
  }


}
