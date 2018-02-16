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

package connectors

import base.SpecBase
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.mvc.Http.Status
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.partials.HtmlPartial.{Failure, Success}
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartials, HtmlPartial}

import scala.concurrent.Future


class ServiceInfoPartialConnectorSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach with ScalaFutures {

  "The ServiceInfoPartialConnector.getServiceInfoPartial() method" when {
    lazy val btaUrl: String = TestServiceInfoPartialConnector.btaUrl

    def result: Future[Html] = TestServiceInfoPartialConnector.getServiceInfoPartial()

    "a valid HtmlPartial is received" should {
      "retrieve the correct HTML" in {

        when(mockHttpGet.GET[HtmlPartial](Matchers.eq(btaUrl))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(successResponse))

        whenReady(result) { response =>
          response mustBe serviceInfoPartialSuccess
        }
      }
    }

    "a BadRequest(400) exception occurs" should {
      "fail and return empty content" in {
        when(mockHttpGet.GET[HtmlPartial](Matchers.eq(btaUrl))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(badRequestResponse))

        whenReady(result) { response =>
          response mustBe Html("")
        }
      }
    }

    "a GatewayTimeout(504) exception occurs" should {
      "fail and return empty content" in {
        when(mockHttpGet.GET[HtmlPartial](Matchers.eq(btaUrl))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(gatewayTimeoutResponse))

        whenReady(result) { response =>
          response mustBe Html("")
        }
      }
    }

    "an unexpected future failed occurs" should {
      "return empty" in {
        val badResponse = HttpResponse(Status.BAD_REQUEST, responseString = Some("Error Message"))

        when(mockHttpGet.GET[HtmlPartial](Matchers.eq(btaUrl))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.failed(new Exception))


        whenReady(result) { response =>
          response mustBe Html("")
        }
      }
    }
  }

  val mockHttpGet: HttpClient = mock[HttpClient]

  object TestServiceInfoPartialConnector extends ServiceInfoPartialConnector(mockHttpGet, frontendAppConfig)

  val serviceInfoPartialSuccess =
    Html(
      """
    <a id="service-info-home-link"
       class="service-info__item service-info__left font-xsmall button button--link button--link-table button--small soft-half--sides"
       data-journey-click="Header:Click:Home"
       href="/business-account">
      Business tax home
    </a>
    <ul id="service-info-list"
      class="service-info__item service-info__right list--collapse">
      <li class="list__item">
        <span id="service-info-user-name" class="bold-xsmall">Test User</span>
      </li>

      <li class="list__item soft--left">
        <a id="service-info-manage-account-link"
           href="/business-account/manage-account"
          data-journey-click="Header:Click:ManageAccount">
          Manage account
        </a>
      </li>
      <li class="list__item soft--left">
        <a id="service-info-messages-link"
           href="/business-account/messages"
          data-journey-click="Header:Click:Messages">
          Messages
        </a>
      </li>
    </ul>
  """.stripMargin.trim)

  val successResponse = Success(None, serviceInfoPartialSuccess)
  val badRequestResponse = Failure(Some(Status.BAD_REQUEST))
  val gatewayTimeoutResponse = Failure(Some(Status.GATEWAY_TIMEOUT))
  val badResponse = HttpResponse(Status.BAD_REQUEST, responseString = Some("Error Message"))
  implicit val hcwc: HeaderCarrierForPartials = HeaderCarrierForPartials(HeaderCarrier(), "")

}