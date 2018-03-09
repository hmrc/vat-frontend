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

package models

import uk.gov.hmrc.domain.Vrn



trait VatEnrolment {
  val vrn: Vrn
  val isActivated: Boolean
}

case class VatDecEnrolment(vrn: Vrn, isActivated: Boolean) extends VatEnrolment

case class VatVarEnrolment(vrn: Vrn, isActivated: Boolean) extends VatEnrolment

//vrn,
//object VatEnrolment {
//
//  def hasActivatedEnrolmentForVat(implicit userProfile: UserProfile): Boolean = hasActivatedEnrolment(VAT_DEC)
//
//  def hasNoEnrolmentForVatVariations(implicit userProfile: UserProfile): Boolean = !hasEnrolment(VAT_VAR)
//
//  def hasNonActivatedEnrolmentForVatVariations(implicit userProfile: UserProfile): Boolean = hasNonActivatedEnrolment(VAT_VAR)
//
//  def hasActivatedEnrolmentForVatVariations(implicit userProfile: UserProfile): Boolean = hasActivatedEnrolment(VAT_VAR)
//
//}
