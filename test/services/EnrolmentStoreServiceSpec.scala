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
import connectors.{EnrolmentStoreConnector, MockHttpClient}
import models.{UserEnrolmentStatus, UserEnrolments, VatVarEnrolment}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class EnrolmentStoreServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfter with MockHttpClient{
  val activeOct13 = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"),  Some(new DateTime("2018-10-13T17:36:00.000Z").toLocalDateTime))
  val activeJan01 = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"),  Some(new DateTime("2018-01-01T17:36:00.000Z").toLocalDateTime))
  val activeFeb28 = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"),  Some(new DateTime("2018-02-28T17:36:00.000Z").toLocalDateTime))
  val noDate = UserEnrolmentStatus("HMRC-VAT-DEC", Some("active"),  None)

  class TestEnrolmentStoreConnector extends EnrolmentStoreConnector{

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {
      Future(Right(UserEnrolments(List(activeOct13))))
    }
  }

  class NoEnrolmentsStatusFound extends EnrolmentStoreConnector{

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {
      Future(Right(UserEnrolments(Nil)))
    }
  }

  class FailedToRetrieveEnrolmentStatus extends EnrolmentStoreConnector{

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {
      Future(Left("Simulated Failure"))
    }
  }

  class TestEnrolmentStoreConnectorWithMultipleEnrolments extends EnrolmentStoreConnector{

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {
      Future(Right(UserEnrolments(List(activeJan01, activeFeb28))))
    }
  }

  class SingleEnrolmentNoDate extends EnrolmentStoreConnector{

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {
      Future(Right(UserEnrolments(List(noDate))))
    }
  }

  class MultipleEnrolmentsNoDate extends EnrolmentStoreConnector{

    override def http: HttpClient = mock[HttpClient]

    override def getEnrolments(credId: String)(implicit headerCarrier: HeaderCarrier): Future[Either[String, UserEnrolments]] = {
      Future(Right(UserEnrolments(List(noDate, noDate))))
    }
  }

  def service = new EnrolmentStoreServiceImpl(new TestEnrolmentStoreConnector)
  def noEnrolments = new EnrolmentStoreServiceImpl(new NoEnrolmentsStatusFound)
  def failedToGetEnrolments = new EnrolmentStoreServiceImpl(new FailedToRetrieveEnrolmentStatus)
  def serviceWithMultipleEnrolments = new EnrolmentStoreServiceImpl(new TestEnrolmentStoreConnectorWithMultipleEnrolments)
  def singleEnrolmentNoDate = new EnrolmentStoreServiceImpl(new SingleEnrolmentNoDate)
  def multipleEnrolmentsNoDate = new EnrolmentStoreServiceImpl(new MultipleEnrolmentsNoDate)

  private val moreThan23DaysFromTokenExpiry = new DateTime("2018-09-15T08:00:00.000").toLocalDate

  private val lessThan23DaysFromTokenExpiry = new DateTime("2018-09-25T08:00:00.000").toLocalDate

  private val exactly23DaysFromTokenExpiry = new DateTime("2018-09-20T17:36:00.000").toLocalDate

  private val multipleRecords = new DateTime("2018-02-01T17:36:00.000").toLocalDate

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  "EnrolmentStoreService" when {

    "showActivationLink is called" should {

      "return false when enrolment was requested within last 7 days" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          moreThan23DaysFromTokenExpiry).futureValue mustBe false

      }

      "return false when enrolment was requested within last 7 days and tax is activated" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = true),
          lessThan23DaysFromTokenExpiry).futureValue mustBe false

      }

      "return false when multiple records are returned and latest enrolment record is within 7 days of current date" in {
        serviceWithMultipleEnrolments.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          multipleRecords).futureValue mustBe false
      }

      "return false when enrolment was requested 7 days ago" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          exactly23DaysFromTokenExpiry).futureValue mustBe false

      }

      "return true when enrolment was requested more than 7 days ago" in {

        service.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          lessThan23DaysFromTokenExpiry).futureValue mustBe true

      }

      "return true if no enrolments were found" in {

        noEnrolments.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          exactly23DaysFromTokenExpiry).futureValue mustBe true

      }

      "return true if enrolments were not able to be retrieved" in {

        failedToGetEnrolments.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          exactly23DaysFromTokenExpiry).futureValue mustBe true

      }

      "return true if for single enrolment with no enrolmentTokenExpiryDate set" in {

        singleEnrolmentNoDate.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          exactly23DaysFromTokenExpiry).futureValue mustBe true

      }

      "return true if for multiple enrolments with no enrolmentTokenExpiryDate set" in {

        multipleEnrolmentsNoDate.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          exactly23DaysFromTokenExpiry).futureValue mustBe true

      }

      "return false if for multiple enrolments with no enrolmentTokenExpiryDate set" in {

        multipleEnrolmentsNoDate.showNewPinLink(VatVarEnrolment(Vrn("credId"), isActivated = false),
          exactly23DaysFromTokenExpiry).futureValue mustBe true

      }

    }
  }
}