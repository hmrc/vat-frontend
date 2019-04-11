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

package connectors

import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.scalatest.mockito.MockitoSugar
import play.api.Play
import play.api.libs.json.Writes
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future


trait MockHttpClient extends MockitoSugar {


  def http(httpWrapper: HttpWrapper) = new HttpClient {

    override def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = Future.successful(httpWrapper.getF(url))

    override def doPatch[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
      Future.successful(httpWrapper.putF(url))

    override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
      Future.successful(httpWrapper.postF(url))

    override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] =
      Future.successful(httpWrapper.postF(url))

    override def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = Future.successful(httpWrapper.postF(url))

    override def doFormPost(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier): Future[HttpResponse] =
      Future.successful(httpWrapper.postF(url))

    override def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = Future.successful(httpWrapper.deleteF(url))

    override def doPut[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
      Future.successful(httpWrapper.patchF(url))

    override val hooks: Seq[HttpHook] = NoneRequired

    override def configuration: Option[Config] = None

    override protected def actorSystem: ActorSystem = ActorSystem()

    def doPutString(url: String,body: String,headers: Seq[(String, String)])
                   (implicit hc: uk.gov.hmrc.http.HeaderCarrier): scala.concurrent.Future[uk.gov.hmrc.http.HttpResponse] = ???
  }

  class HttpWrapper {
    def getF[T](uri: String): HttpResponse = HttpResponse(200, None)

    def postF[T](uri: String): HttpResponse = HttpResponse(200, None)

    def putF[T](uri: String): HttpResponse = ???

    def deleteF[T](uri: String): HttpResponse = ???

    def patchF[T](uri: String): HttpResponse = ???
  }

}