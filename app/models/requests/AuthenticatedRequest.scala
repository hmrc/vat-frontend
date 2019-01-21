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

package models.requests

import models.{VatDecEnrolment, VatEnrolment, VatVarEnrolment}
import play.api.mvc.{Request, WrappedRequest}

case class AuthenticatedRequest[A](request: Request[A],
                                   externalId: String,
                                   vatDecEnrolment: VatDecEnrolment,
                                   vatVarEnrolment: VatEnrolment) extends WrappedRequest[A](request) {

  def hasActivatedEnrolmentForVat: Boolean = vatDecEnrolment.isActivated

  def hasNoEnrolmentForVatVariations: Boolean = !vatVarEnrolment.enrolled

  def hasNonActivatedEnrolmentForVatVariations: Boolean = vatVarEnrolment.enrolled && !vatVarEnrolment.isActivated

  def hasActivatedEnrolmentForVatVariations: Boolean = vatVarEnrolment.enrolled && vatVarEnrolment.isActivated
}
