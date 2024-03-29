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

package services

import base.SpecBase
import connectors.{EnrolmentStoreConnector, MockHttpClient}
import models.requests.AuthenticatedRequest
import models.{UserEnrolmentStatus, UserEnrolments, VatDecEnrolment, VatVarEnrolment, Vrn}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http._

import java.time.{LocalDateTime, OffsetDateTime}
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentStoreServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfter with MockHttpClient {

  val activeOct13 = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"), Some(LocalDateTime.parse("2018-10-13T23:59:59.999")))
  val activeJan01 = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"), Some(LocalDateTime.parse("2018-01-01T23:59:59.999")))
  val activeFeb28 = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"), Some(LocalDateTime.parse("2018-02-28T23:59:59.999")))
  val noDate = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"), None)

  implicit val request: Request[_] = Request(
    AuthenticatedRequest(fakeRequest, "", VatDecEnrolment(Vrn(""), isActivated = true), vatVarEnrolment = VatVarEnrolment(Vrn(""), isActivated = true), credId = ""),
    HtmlFormat.empty
  )

  class TestEnrolmentStoreConnector extends EnrolmentStoreConnector {

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
      Future.successful(Right(UserEnrolments(List(activeOct13))))
    }
  }

  class NoEnrolmentsStatusFound extends EnrolmentStoreConnector {

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
      Future.successful(Right(UserEnrolments(Nil)))
    }
  }

  class FailedToRetrieveEnrolmentStatus extends EnrolmentStoreConnector {

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
      Future.successful(Left("Simulated Failure"))
    }
  }

  class TestEnrolmentStoreConnectorWithMultipleEnrolments extends EnrolmentStoreConnector {

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
      Future.successful(Right(UserEnrolments(List(activeJan01, activeFeb28))))
    }
  }

  class SingleEnrolmentNoDate extends EnrolmentStoreConnector {

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
      Future.successful(Right(UserEnrolments(List(noDate))))
    }
  }

  class MultipleEnrolmentsNoDate extends EnrolmentStoreConnector {

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Either[String, UserEnrolments]] = {
      Future.successful(Right(UserEnrolments(List(noDate, noDate))))
    }
  }

  def service = new EnrolmentStoreServiceImpl(new TestEnrolmentStoreConnector)

  def noEnrolments = new EnrolmentStoreServiceImpl(new NoEnrolmentsStatusFound)

  def failedToGetEnrolments = new EnrolmentStoreServiceImpl(new FailedToRetrieveEnrolmentStatus)

  def serviceWithMultipleEnrolments = new EnrolmentStoreServiceImpl(new TestEnrolmentStoreConnectorWithMultipleEnrolments)

  def singleEnrolmentNoDate = new EnrolmentStoreServiceImpl(new SingleEnrolmentNoDate)

  def multipleEnrolmentsNoDate = new EnrolmentStoreServiceImpl(new MultipleEnrolmentsNoDate)

  val dtf2: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  private val moreThan23DaysFromTokenExpiry = OffsetDateTime.parse("2018-09-15T08:00:00.000Z", dtf2)

  private val lessThan23DaysFromTokenExpiry = OffsetDateTime.parse("2018-09-25T08:00:00.000Z", dtf2)

  private val exactly23DaysFromTokenExpiry = OffsetDateTime.parse("2018-09-20T23:59:59.999Z", dtf2)

  private val multipleRecords = OffsetDateTime.parse("2018-02-01T17:36:00.000Z", dtf2)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "EnrolmentStoreService" when {

    "showActivationLink is called" should {

      "return false when enrolment was requested within last 7 days" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          moreThan23DaysFromTokenExpiry, "credId").futureValue mustBe false

      }

      "return false when enrolment was requested within last 7 days and tax is activated" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = true),
          lessThan23DaysFromTokenExpiry, "credId").futureValue mustBe false

      }

      "return false when multiple records are returned and latest enrolment record is within 7 days of current date" in {
        serviceWithMultipleEnrolments.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          multipleRecords, "credId").futureValue mustBe false
      }

      "return false when enrolment was requested 7 days ago" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          exactly23DaysFromTokenExpiry, "credId").futureValue mustBe false

      }

      "return true when enrolment was requested more than 7 days ago" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          lessThan23DaysFromTokenExpiry, "credId").futureValue mustBe true

      }

      "return true if no enrolments were found" in {

        noEnrolments.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          exactly23DaysFromTokenExpiry, "credId").futureValue mustBe true

      }

      "return true if enrolments were not able to be retrieved" in {

        failedToGetEnrolments.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          exactly23DaysFromTokenExpiry, "credId").futureValue mustBe true

      }

      "return true if for single enrolment with no enrolmentTokenExpiryDate set" in {

        singleEnrolmentNoDate.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          exactly23DaysFromTokenExpiry, "credId").futureValue mustBe true

      }

      "return true if for multiple enrolments with no enrolmentTokenExpiryDate set" in {

        multipleEnrolmentsNoDate.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          exactly23DaysFromTokenExpiry, "credId").futureValue mustBe true

      }

      "return false if for multiple enrolments with no enrolmentTokenExpiryDate set" in {

        multipleEnrolmentsNoDate.showNewPinLink(VatVarEnrolment(Vrn("vrn"), isActivated = false),
          exactly23DaysFromTokenExpiry, "credId").futureValue mustBe true

      }

    }
  }
}