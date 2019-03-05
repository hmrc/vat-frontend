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

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import connectors.models.{VatAccountData, VatData}
import models.VatEnrolment
import models.requests.AuthenticatedRequest
import play.api.i18n.{Lang, Messages}
import play.twirl.api.Html

@ImplementedBy(classOf[ReturnsPartialBuilderImpl])
trait ReturnsPartialBuilder{
  def buildReturnsPartial(vatAccountData: VatAccountData, vatEnrolment: VatEnrolment)(implicit messages: Messages,
                                                                                      lang: Lang, request: AuthenticatedRequest[_]): Html
}

@Singleton
class ReturnsPartialBuilderImpl @Inject()(appConfig: FrontendAppConfig) extends ReturnsPartialBuilder {
  def buildReturnsPartial(vatAccountData: VatAccountData, vatEnrolment: VatEnrolment)(implicit messages: Messages,
                                                                                      lang: Lang, request: AuthenticatedRequest[_]): Html = {
    vatAccountData match {
      case VatData(account, _) if account.openPeriods.isEmpty => views.html.partials.card_partials.no_returns(appConfig, Some(vatEnrolment))
      case VatData(account, _) if account.openPeriods.length == 1 => views.html.partials.card_partials.one_return(appConfig, Some(vatEnrolment))
      case VatData(account, _) if account.openPeriods.length > 1 => views.html.partials.card_partials.multiple_returns(appConfig, Some(vatEnrolment),
        account.openPeriods.length)
      case _ => Html("")
    }
  }
}
