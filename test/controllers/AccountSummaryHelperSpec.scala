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

import connectors.models.{CtAccountBalance, CtAccountSummaryData}
import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import services.CtService
import uk.gov.hmrc.domain.CtUtr
import views.ViewSpecBase
import views.html.partials.not_activated
import views.html.subpage

import scala.concurrent.Future

class AccountSummaryHelperSpec extends ViewSpecBase with MockitoSugar with ScalaFutures {


  val accountSummary = Html("Account Summary")
  val mockAccountSummaryHelper: AccountSummaryHelper = mock[AccountSummaryHelper]
  when(mockAccountSummaryHelper.getAccountSummaryView(Matchers.any())).thenReturn(Future.successful(accountSummary))

  val mockCtService: CtService = mock[CtService]
  when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(CtNoData))

  def accountSummaryHelper() = new AccountSummaryHelper(frontendAppConfig, mockCtService, messagesApi)


  def ctEnrolment(activated: Boolean = true) =  CtEnrolment(CtUtr("utr"), isActivated = true)
  def requestWithEnrolment(activated: Boolean): AuthenticatedRequest[AnyContent] = {
    AuthenticatedRequest[AnyContent](FakeRequest(), "", ctEnrolment(activated))
  }

  val fakeRequestWithEnrolments: AuthenticatedRequest[AnyContent] = requestWithEnrolment(activated = true)

  def viewAsString(balanceInformation: String = ""): String =
    subpage(frontendAppConfig, ctEnrolment(), accountSummary)(HtmlFormat.empty)(fakeRequestWithEnrolments, messages).toString

  "getAccountSummaryView" when {
    "there is no account summary data" should {
      "return 'No Balance information to display'" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(CtNoData))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          view.toString must include("No balance information to display")
        }
      }
    }
    "there is an error retrieving the data" should {
      "return the generic error message" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(CtGenericError))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          view.toString must include("We can’t display your Corporation Tax information at the moment.")
        }
      }
    }

    "the user has no CT information available" should {
      "return the generic error message" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(CtEmpty))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          view.toString must include("We can’t display your Corporation Tax information at the moment.")
        }
      }
    }

    "the user has an unactivated enrolment" should {
      "return the not activated view" in {
        val notActivated = not_activated(
          "http://localhost:8080/portal/service/corporation-tax?action=activate&step=enteractivationpin&lang=eng",
          "http://localhost:8080/portal/service/corporation-tax?action=activate&step=requestactivationpin&lang=eng"
        )(fakeRequest, messages)

        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any())).thenReturn(Future.successful(CtUnactivated))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          view mustBe notActivated
        }
      }
    }

    "the user has a null balance" should {
      "return Nothing to pay" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CtData(CtAccountSummaryData(Some(CtAccountBalance(None))))))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          asDocument(view).text() must include("You have nothing to pay - view statement")
        }
      }
    }

    "the user has a balance of 0" should {
      "return Nothing to pay" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CtData(CtAccountSummaryData(Some(CtAccountBalance(Some(0)))))))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          asDocument(view).text() must include("You have nothing to pay - view statement")
        }
      }
    }

    "the user is in credit" should {
      "return You are in credit" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CtData(CtAccountSummaryData(Some(CtAccountBalance(Some(-123.45)))))))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          asDocument(view).text() must include("You are £123.45 in credit - see breakdown")
        }
      }
    }

    "the user owes money" should {
      "return You owe money" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CtData(CtAccountSummaryData(Some(CtAccountBalance(Some(999.99)))))))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          asDocument(view).text() must include("You owe £999.99 - see breakdown")
        }
      }
    }

    "the breakdown link" should {
      "direct the user to the breakdown page" in {
        reset(mockCtService)
        when(mockCtService.fetchCtModel(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CtData(CtAccountSummaryData(Some(CtAccountBalance(Some(999.99)))))))
        whenReady(accountSummaryHelper().getAccountSummaryView(fakeRequestWithEnrolments)) { view =>
          assertLinkById(asDocument(view), "ct-see-breakdown", "see breakdown",
            "http://localhost:8080/portal/corporation-tax/org/utr/account/balanceperiods?lang=eng",
            "corporation-tax:Click:SeeBreakdown")
        }
      }
    }
  }

}
