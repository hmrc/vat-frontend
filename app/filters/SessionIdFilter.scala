/*
 * Copyright 2020 HM Revenue & Customs
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

package filters

import java.util.UUID

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.http.HeaderNames
import play.api.mvc._
import uk.gov.hmrc.http.{SessionKeys, HeaderNames => HMRCHeaderNames}

import scala.concurrent.{ExecutionContext, Future}

class SessionIdFilter(
                       uuid: => UUID,
                       sessionCookieBaker: SessionCookieBaker,
                       cookieHeaderEncoding: CookieHeaderEncoding,
                       override val mat: Materializer,
                       implicit val ec: ExecutionContext
                     ) extends Filter {

  @Inject
  def this(sessionCookieBaker: SessionCookieBaker,
           cookieHeaderEncoding: CookieHeaderEncoding,
           mat: Materializer,
           ec: ExecutionContext) {
    this(UUID.randomUUID(), sessionCookieBaker, cookieHeaderEncoding, mat, ec)
  }

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {

    lazy val sessionId: String = s"session-$uuid"

    if (rh.session.get(SessionKeys.sessionId).isEmpty) {
      val cookies: String = {
        val session: Session =
          rh.session + (SessionKeys.sessionId -> sessionId)

        val cookies: Traversable[Cookie] =
          rh.cookies ++ Seq(sessionCookieBaker.encodeAsCookie(session))

        cookieHeaderEncoding.encodeCookieHeader(cookies.toSeq)
      }

      val headers: Headers = rh.headers.add(
        HMRCHeaderNames.xSessionId -> sessionId,
        HeaderNames.COOKIE -> cookies
      )

      f(rh.withHeaders(newHeaders = headers)).map {
        result =>
          val cookies: Cookies =
            cookieHeaderEncoding.fromSetCookieHeader(result.header.headers.get(HeaderNames.SET_COOKIE))

          val session: Session =
            sessionCookieBaker.decodeFromCookie(cookies.get(sessionCookieBaker.COOKIE_NAME)).data
              .foldLeft(rh.session)(_ + _)

          result.withSession(session + (SessionKeys.sessionId -> sessionId))
      }
    } else {
      f(rh)
    }
  }
}

