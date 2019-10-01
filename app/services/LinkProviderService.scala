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

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.models.VatData
import models.requests.AuthenticatedRequest
import models.{Annually, Calendar, InactiveDirectDebit, Link}
import play.api.i18n.Messages

class LinkProviderService @Inject()(appConfig: FrontendAppConfig) {

  private def makePaymentLink(implicit messages: Messages) = Link(
    id = "vat-make-payment-link",
    title = messages("card.vat.payments.make_a_vat_payment"),
    href = appConfig.getUrl("makeAPayment"),
    ga = "link - click:VAT cards:Make a VAT payment"
  )

  private def setUpDirectDebitLink(implicit messages: Messages, request: AuthenticatedRequest[_]) = Link(
    id = "vat-direct-debit-setup-link",
    title = messages("card.vat.payments.set_up_a_vat_direct_debit"),
    href = appConfig.getPortalUrl("vatOnlineAccount")(Some(request.vatDecEnrolment)),
    ga = "link - click:VAT cards:Set up a VAT Direct Debit"
  )

  def determinePaymentAdditionalLinks(
                                       vatData: VatData
                                     )(
                                       implicit messages: Messages, request: AuthenticatedRequest[_]
                                     ): Option[List[Link]] = {
    vatData.accountSummary.accountBalance.flatMap(_.amount) match {
      case Some(amount) if amount > 0 => vatData.calendar match {
        case Some(Calendar(filingFrequency, InactiveDirectDebit)) if filingFrequency != Annually =>
          Some(List(makePaymentLink, setUpDirectDebitLink))
        case _ => Some(List(makePaymentLink))
      }
      case _ => None
    }
  }

}
