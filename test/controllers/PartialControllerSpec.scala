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

package controllers

import connectors.models.{AccountSummaryData, VatModel}
import controllers.actions._
import models.requests.AuthenticatedRequest
import models.{Helper, VatDecEnrolment, VatEnrolment, VatNoEnrolment}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Vrn
import views.html.partial

import scala.concurrent.Future
import scala.util.Success


class PartialControllerSpec extends ControllerSpecBase with MockitoSugar {

  //TODO: Needs VatModel
  val vatModel = VatModel(Success(Some(AccountSummaryData(None, None))), None)
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  val mockHelper = mock[Helper]
  when(mockAccountSummaryHelper.getVatModel(Matchers.any())).thenReturn(Future.successful(vatModel))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new PartialController(messagesApi, FakeAuthAction, FakeServiceInfoAction, mockAccountSummaryHelper, mockHelper, frontendAppConfig)

  def vrnEnrolment(activated: Boolean = true) =  VatDecEnrolment(Vrn("vrn"), isActivated = true)

  def requestWithEnrolment(activated: Boolean): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", vrnEnrolment(activated), VatNoEnrolment())
  }

  def viewAsString() = partial(Vrn("vrn"), vatModel, mockHelper,frontendAppConfig)(fakeRequest, messages, requestWithEnrolment(true)).toString

  "Partial Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}




