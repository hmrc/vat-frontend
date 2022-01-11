/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.libs.json.Writes
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  val mockConfig = mock[Config]

  val mockHttpClient: HttpClient = spy(new TestHttpClient(mockConfig))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient)
  }

  def mockGet(specificUrl: Option[String], mockedResponse: Future[HttpResponse]): Unit = {
    val core = doAnswer(_ => mockedResponse).when(mockHttpClient)

    specificUrl.fold {
      core.doGet(any(), any())(any())
    } {
      url => core.doGet(eqTo(url), any())(any())
    }
  }

  def mockPost(specificUrl: Option[String], mockedResponse: Future[HttpResponse]): Unit = {
    val core = doAnswer(_ => mockedResponse).when(mockHttpClient)

    specificUrl.fold {
      core.doPost(any(), any(), any())(any(), any())
    } {
      url => core.doPost(eqTo(url), any(), any())(any(), any())
    }
  }

  def mockGet(specificUrl: Option[String])(mockedResponse: HttpResponse): Unit = mockGet(specificUrl, Future.successful(mockedResponse))

  def mockPost(specificUrl: Option[String])(mockedResponse: HttpResponse): Unit = mockPost(specificUrl, Future.successful(mockedResponse))

  def verifyGet(url: String)(wanted: Int): Unit =
    verify(mockHttpClient, times(wanted)).GET(eqTo(url), any(), any())(any(), any(), any())


  class HttpWrapper {
    def getF[T](uri: String): HttpResponse = HttpResponse.apply(200, None.toString)

    def postF[T](uri: String): HttpResponse = HttpResponse.apply(200, None.toString)

    def putF[T](uri: String): HttpResponse = ???

    def deleteF[T](uri: String): HttpResponse = ???

    def patchF[T](uri: String): HttpResponse = ???
  }

}

private class TestHttpClient(config: Config) extends HttpClient {
  override val hooks: Seq[HttpHook] = NoneRequired

  override def configuration: Config = config

  override protected def actorSystem: ActorSystem = ActorSystem()

  def doPutString(url: String, body: String, headers: Seq[(String, String)])
                 (implicit hc: uk.gov.hmrc.http.HeaderCarrier): scala.concurrent.Future[uk.gov.hmrc.http.HttpResponse] = ???

  override def doDelete(url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???

  override def doPatch[A](url: String, body: A, headers: Seq[(String, String)])(implicit rds: Writes[A], ec: ExecutionContext): Future[HttpResponse] = ???

  override def doGet(url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???

  override def doPut[A](url: String, body: A, headers: Seq[(String, String)])(implicit rds: Writes[A], ec: ExecutionContext): Future[HttpResponse] = ???

  override def doPutString(url: String, body: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???

  override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], ec: ExecutionContext): Future[HttpResponse] = ???

  override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???

  override def doEmptyPost[A](url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???

  override def doFormPost(url: String, body: Map[String, Seq[String]], headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???
}
