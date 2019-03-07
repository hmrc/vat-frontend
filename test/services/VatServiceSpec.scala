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
import connectors.models.designatorydetails.DesignatoryDetailsCollection
import connectors.{MockHttpClient, VatConnector}
import models._
import org.joda.time.LocalDate
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfter with MockHttpClient {

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  lazy val mockVatConnector: VatConnector = mock[VatConnector]

  lazy val service = new VatService(mockVatConnector)

  lazy val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
  lazy val vatCalendarData: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(true, None), None, Seq()))
  lazy val vatCalendar: Option[Calendar] = Some(Calendar( filingFrequency = Monthly, directDebit = InactiveDirectDebit))
  lazy val accountSummaryAndCalendar: VatAccountData = VatData(vatAccountSummary, vatCalendar)

  lazy val vatEnrolment = VatDecEnrolment(Vrn("utr"), isActivated = true)

  lazy val dDActive = DirectDebitActive(new LocalDate(2016, 6, 30), new LocalDate(2016, 8, 15))

  before{
    reset(mockVatConnector)
  }

  "The VatService fetchVatModel method" when {

    "the connector returns data" should {
      "return VatData" in {
        when(mockVatConnector.accountSummary(vatEnrolment.vrn)).thenReturn(Future.successful(Option(vatAccountSummary)))
        when(mockVatConnector.calendar(vatEnrolment.vrn)).thenReturn(Future.successful(vatCalendarData))
        whenReady(service.fetchVatModel(Some(vatEnrolment))) {
          _ mustBe accountSummaryAndCalendar
        }
      }
    }

    "the connector returns no data" should {
      "return VatNotFoundError" in {
        when(mockVatConnector.accountSummary(vatEnrolment.vrn)).thenReturn(Future.successful(None))
        whenReady(service.fetchVatModel(Some(vatEnrolment))) {
          _ mustBe VatNoData
        }
      }
    }

    "the connector throws an exception" should {
      "return VatGenericError" in {
        when(mockVatConnector.accountSummary(vatEnrolment.vrn)).thenReturn(Future.failed(new Throwable))
        whenReady(service.fetchVatModel(Some(vatEnrolment))) {
          _ mustBe VatGenericError
        }
      }
    }

    "the vat enrolment is empty" should {
      "return a VatEmpty" in {
        whenReady(service.fetchVatModel(None)) {
          _ mustBe VatEmpty
        }
      }
    }

    "the vat enrolment is not activated" should {
      "return a VatUnactivated" in {
        whenReady(service.fetchVatModel(Some(VatDecEnrolment(Vrn("vrn"), isActivated = false)))) {
          _ mustBe VatUnactivated
        }
      }
    }

  }

  "The VatService designatoryDetails method" when {

    "the connector returns designatory details" should {
      "return VatDesignatoryDetailsCollection" in {
        val designatoryDetails = Some(DesignatoryDetailsCollection(None, None))
        when(mockVatConnector.designatoryDetails(vatEnrolment.vrn)).thenReturn(Future.successful(designatoryDetails))

        whenReady(service.designatoryDetails(vatEnrolment)) {
          _ mustBe designatoryDetails
        }
      }
    }

    "the connector returns an exception" should {
      "return None when designatoryDetails call throws an exception" in {
        when(mockVatConnector.designatoryDetails(vatEnrolment.vrn)).thenReturn(Future.failed(new Throwable))

        whenReady(service.designatoryDetails(vatEnrolment)) {
          _ mustBe None
        }
      }
    }

  }

  "The VatService calendar method" when {

    def calendarSetup(code: String): Unit = {
      when(mockVatConnector.calendar(vatEnrolment.vrn)).thenReturn(Future.successful(Some(CalendarData(Some(code), DirectDebit(true, None), None, Seq()))))
    }

    "the stagger code is 0000" should {
      "have Monthly as the filing frequency" in {
        calendarSetup("0000")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Monthly
        }
      }
    }

    "the stagger code is 0001" should {
      "have Quarterly (March) as the filing frequency" in {
        calendarSetup("0001")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Quarterly(March)
        }
      }
    }

    "the stagger code is 0002" should {
      "have Quarterly (January) as the filing frequency" in {
        calendarSetup("0002")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Quarterly(January)
        }
      }
    }

    "the stagger code is 0003" should {
      "have Quarterly (February) as the filing frequency" in {
        calendarSetup("0003")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Quarterly(February)
        }
      }
    }

    "the stagger code is between 0004 and 0015" should {
        (4 to 15) foreach { i =>
          val formattedString = "%04d".format(i)
          s"have Annually as the filing frequency for $formattedString code" in {
            calendarSetup(formattedString)
          whenReady(service.vatCalendar(vatEnrolment)) {
            _.get.filingFrequency mustBe Annually
          }
        }
      }
    }

    "the stagger code is invalid" should {
      "return with a FilingFrequency of InvalidStaggerCode" in {
        calendarSetup("0016")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe InvalidStaggerCode
        }
      }
    }

    def directDebitSetup(directDebitFromConnector: DirectDebit): Unit = {
      when(mockVatConnector.calendar(vatEnrolment.vrn)).thenReturn(Future.successful(Some(CalendarData(Some("0000"), directDebitFromConnector, None, Seq()))))
    }

    "The user is not eligible for direct debit" should {
      "return with a direct debit property of DirectDebitIneligible" in {
        directDebitSetup(DirectDebit(false,None))
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.directDebit mustBe DirectDebitIneligible
        }
      }

      "The user is eligible for direct debit, but has not activated" should {
        "return with a direct debit property of InactiveDirectDebit" in {
          directDebitSetup(DirectDebit(true,None))
          whenReady(service.vatCalendar(vatEnrolment)) {
            _.get.directDebit mustBe InactiveDirectDebit
          }
        }
      }

      "The user is paying by direct debit" should {
        "return with a direct debit property of ActiveDirectDebit, holding the user's details" in {
          directDebitSetup(DirectDebit(true,Some(dDActive)))
          whenReady(service.vatCalendar(vatEnrolment)) {
            _.get.directDebit mustBe ActiveDirectDebit(dDActive)
          }
        }

        class testBrokenVatConnector(http: HttpClient, config: FrontendAppConfig) extends VatConnector(http, config){
          override def calendar(vrn: Vrn)(implicit hc: HeaderCarrier): Future[Option[CalendarData]] = {
            Future.failed(new Exception("test exception"))
          }
        }

        "The connector throws an exception" should {
          "return an empty option" in {
            val httpClient = http(new HttpWrapper)
            val brokenService = new VatService(new testBrokenVatConnector(httpClient, frontendAppConfig))
            whenReady(brokenService.vatCalendar(vatEnrolment)) {
             _ mustBe None
            }
          }
        }

        "The connector returns no data" should {
          "return an empty option" in {
            when(mockVatConnector.calendar(vatEnrolment.vrn)).thenReturn(Future.successful(None))
            whenReady(service.vatCalendar(vatEnrolment)) {
              _ mustBe None
            }
          }
        }
      }

    }

  }
}
