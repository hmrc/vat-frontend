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

package utils

import javax.inject.Inject

import config.FrontendAppConfig
import models.{VatDecEnrolment, VatEnrolment}
import play.api.mvc.Request
import uk.gov.hmrc.domain.Vrn

class EmacUrlBuilder@Inject()(appConfig: FrontendAppConfig) {

  def getRequestAccessUrl(enrolmentKey: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): String = {

    val vatDecEnrolment = VatDecEnrolment(Vrn("a-users-vrn"), isActivated = true)

    if (appConfig.useEmacVatEnrolment)
      s"/enrolment-management-frontend/HMCE-VATVAR-ORG/request-access-tax-scheme?continue=%2Fbusiness-account"
    else {
      appConfig.getPortalUrl(enrolmentKey)(Some(vatDecEnrolment))
    }
  }
}
