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

package services

import base.SpecBase
import connectors.VatConnector
import connectors.models._
import models._
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class VatServiceSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  val mockVatConnector: VatConnector = mock[VatConnector]

  val service = new VatService(mockVatConnector)

  val vatAccountSummary: AccountSummaryData = AccountSummaryData(None, None, Seq())
  val vatCalendarData: Option[CalendarData] = Some(CalendarData(Some("0000"), DirectDebit(true, None), None, Seq()))
  val vatCalendar: Option[Calendar] = Some(Calendar(Monthly, DirectDebit(true, None)))
  val accountSummaryAndCalendar: VatAccountData = VatData(vatAccountSummary, vatCalendar)

  val vatEnrolment = VatDecEnrolment(Vrn("utr"), isActivated = true)
  "The VatService fetchVatModel method" when {
    "the connector return data" should {
      "return VatData" in {
        reset(mockVatConnector)
        when(mockVatConnector.accountSummary(vatEnrolment.vrn)).thenReturn(Future.successful(Option(vatAccountSummary)))
        when(mockVatConnector.calendar(vatEnrolment.vrn)).thenReturn(Future.successful(vatCalendarData))
        whenReady(service.fetchVatModel(Some(vatEnrolment))) {
          _ mustBe accountSummaryAndCalendar
        }
      }
    }
    "the connector returns no data" should {
      "return VatNotFoundError" in {
        reset(mockVatConnector)
        when(mockVatConnector.accountSummary(vatEnrolment.vrn)).thenReturn(Future.successful(None))
        whenReady(service.fetchVatModel(Some(vatEnrolment))) {
          _ mustBe VatNoData
        }
      }
    }
    "the connector throws an exception" should {
      "return VatGenericError" in {
        reset(mockVatConnector)
        when(mockVatConnector.accountSummary(vatEnrolment.vrn)).thenReturn(Future.failed(new Throwable))
        whenReady(service.fetchVatModel(Some(vatEnrolment))) {
          _ mustBe VatGenericError
        }
      }
    }
    "the vat enrolment is empty" should {
      "return a VatEmpty" in {
        reset(mockVatConnector)

        whenReady(service.fetchVatModel(None)) {
          _ mustBe VatEmpty
        }
      }
    }
    "the vat enrolment is not activated" should {
      "return a VatUnactivated" in {
        reset(mockVatConnector)
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
      reset(mockVatConnector)
      when(mockVatConnector.calendar(vatEnrolment.vrn)).thenReturn(Future.successful(Some(CalendarData(Some(code), DirectDebit(true, None), None, Seq()))))
    }


    "the stagger code is 0000" should {
      "should have Monthly as the filing frequency" in {
        calendarSetup("0000")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Monthly
        }
      }
    }
    "the stagger code is 0001" should {
      "should have Quarterly (March) as the filing frequency" in {
        calendarSetup("0001")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Quarterly(March)
        }
      }
    }
    "the stagger code is 0002" should {
      "should have Quarterly (January) as the filing frequency" in {
        calendarSetup("0002")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe Quarterly(January)
        }
      }
    }
    "the stagger code is 0003" should {
      "should have Quarterly (February) as the filing frequency" in {
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
      "should be None" in {
        calendarSetup("0016")
        whenReady(service.vatCalendar(vatEnrolment)) {
          _.get.filingFrequency mustBe InvalidStaggerCode
        }
      }
    }
  }
}
