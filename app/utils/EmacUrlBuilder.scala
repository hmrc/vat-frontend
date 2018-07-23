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

import javax.inject.{Inject, Singleton}

import config.FrontendAppConfig
import models.VatEnrolment
import play.api.mvc.Request

@Singleton
class EmacUrlBuilder@Inject()(appConfig: FrontendAppConfig) {

  def getEnrolmentUrl(enrolmentKey: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): String =
    if (appConfig.useEmacVatEnrolment) appConfig.emacVatEnrolmentUrl
    else appConfig.getPortalUrl(enrolmentKey)(vatEnrolment)

  def getActivationUrl(enrolmentKey: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): String =
    if (appConfig.useEmacVatActivation) appConfig.emacVatActivationUrl
    else appConfig.getPortalUrl(enrolmentKey)(vatEnrolment)

  def getLostPinUrl(enrolmentKey: String)(vatEnrolment: Option[VatEnrolment])(implicit request: Request[_]): Option[String] =
    if (appConfig.useEmacVatActivation) Some(appConfig.emacVatLostPinUrl)
    else None
}
