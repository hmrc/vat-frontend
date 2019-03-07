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

import base.SpecBase
import config.FrontendAppConfig
import connectors.models._
import controllers.actions.ServiceInfoAction
import controllers.helpers.AccountSummaryHelper
import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.Future
import models.Card._

import scala.concurrent.ExecutionContext.Implicits.global


object VatPartialBuilderTest extends VatPartialBuilder {
  override def buildReturnsPartial(accountData: VatAccountData, enrolment: VatEnrolment)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Returns partial")
  override def buildPaymentsPartial(accountData: VatAccountData)(implicit request: AuthenticatedRequest[_], messages: Messages): Html = Html("Payments partial")
}

class VatCardBuilderServiceSpec extends SpecBase with ScalaFutures with MockitoSugar {


  class VatCardBuilderServiceTest(messagesApi: MessagesApi,
                                  testVatPartialBuilder: VatPartialBuilder,
                                  testServiceInfo: ServiceInfoAction,
                                  testAccountSummaryHelper: AccountSummaryHelper,
                                  testAppConfig: FrontendAppConfig,
                                  testVatService: VatServiceInterface//,
                                 // testReturnsPartialBuilder: ReturnsPartialBuilder
                                 ) extends VatCardBuilderServiceImpl(messagesApi, testVatPartialBuilder, testServiceInfo, testAccountSummaryHelper, testAppConfig, testVatService
    //, testReturnsPartialBuilder
  )


  trait LocalSetup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    lazy val vatEnrolment: VatDecEnrolment =  VatDecEnrolment(Vrn("123456789"), isActivated = true)
    def authenticatedRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(request = FakeRequest(), externalId = "", vatDecEnrolment = vatEnrolment, vatVarEnrolment = VatNoEnrolment())

    lazy val testVatPartialBuilder: VatPartialBuilder = VatPartialBuilderTest
    lazy val testServiceInfo = mock[ServiceInfoAction]
    lazy val testAccountSummaryHelper = mock[AccountSummaryHelper]
    lazy val testAppConfig = mock[FrontendAppConfig]
    lazy val testVatService: VatServiceInterface = mock[VatServiceInterface]
    lazy val testReturnsPartialBuilder: ReturnsPartialBuilder = new ReturnsPartialBuilder {
      override def buildReturnsPartial(vatAccountData: VatAccountData, vatEnrolment: VatEnrolment)(implicit messages: Messages, lang: Lang, request: AuthenticatedRequest[_]): Html = Html("")

    }

    lazy val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
    lazy val vatCalendarData: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(true, None), None, Seq()))
    lazy val vatCalendar: Option[Calendar] = Some(Calendar( filingFrequency = Monthly, directDebit = InactiveDirectDebit))
    lazy val vatData: VatAccountData = VatData(vatAccountSummary, vatCalendar)

    lazy val testCard: Card = Card(
      title = "VAT",
      referenceNumber = "123456789",
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          ga = "link - click:Your business taxes cards:More VAT details",
          dataSso = None,
          external = false
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = Some("Payments partial"),
      returnsPartial = Some("Returns partial")
    )

    lazy val testCardNoData: Card = Card(
      title = "VAT",
      referenceNumber = "123456789",
      primaryLink = Some(
        Link(
          id = "vat-account-details-card-link",
          title = "VAT",
          href = "http://someTestUrl",
          ga = "link - click:Your business taxes cards:More VAT details",
          dataSso = None,
          external = false
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = Some("\n\n<p role = \"text\">There is no balance information to display.</p>\n"),
      returnsPartial = Some("")
    )

    lazy val service: VatCardBuilderServiceTest = new VatCardBuilderServiceTest(messagesApi, testVatPartialBuilder, testServiceInfo, testAccountSummaryHelper, testAppConfig, testVatService//, testReturnsPartialBuilder
         )

    when(testAppConfig.getUrl("mainPage")).thenReturn("http://someTestUrl")
  }


  "Calling VatCardBuilderService.buildVatCard" should {
    "return a card with No Payments information when getting VatNoData" in new LocalSetup {
      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(VatNoData))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCardNoData
    }

    "return a card with Payment information when getting " in new LocalSetup {
      when(testVatService.fetchVatModel(Some(vatEnrolment))).thenReturn(Future.successful(vatData))

      val result: Future[Card] = service.buildVatCard()(authenticatedRequest, hc, messages)

      result.futureValue mustBe testCard
    }
  }

}
