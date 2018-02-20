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

import javax.inject.Inject

import config.FrontendAppConfig
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import services.VatService
import uk.gov.hmrc.play.HeaderCarrierConverter
import views.html.partials.generic_error
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext.fromLoggingDetails

import scala.concurrent.Future

class AccountSummaryHelper @Inject()(
                                      appConfig: FrontendAppConfig,
                                      vatService: VatService,
                                      override val messagesApi: MessagesApi
                                    ) extends I18nSupport {

  private[controllers] def getAccountSummaryView(implicit r: AuthenticatedRequest[_]) = {

    implicit def hc(implicit rh: RequestHeader) = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))

    Future(generic_error(appConfig.getPortalUrl("home")(r.vatEnrolment)))

  }
}
