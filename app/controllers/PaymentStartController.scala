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

package controllers

import config.FrontendAppConfig
import connectors.models.{AccountBalance, AccountSummaryData, VatData}
import connectors.payments.{PayConnector, SpjRequestBtaVat, VatPeriod}
import controllers.actions._
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}
import services.VatService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import PaymentStartController.toAmountInPence

import scala.concurrent.ExecutionContext

object PaymentStartController {
  def toAmountInPence(amountInPounds: BigDecimal): Long = (amountInPounds * 100).toLong
}

class PaymentStartController @Inject()(appConfig: FrontendAppConfig,
                                       payConnector: PayConnector,
                                       authenticate: AuthAction,
                                       vatService: VatService,
                                       serviceInfo: ServiceInfoAction)(implicit ec: ExecutionContext) extends FrontendController {

  def makeAPayment: Action[AnyContent] = (authenticate andThen serviceInfo).async {
    implicit request =>
      vatService.fetchVatModel(Some(request.request.vatDecEnrolment)).flatMap {
        case VatData(AccountSummaryData(Some(AccountBalance(Some(amount))), _, openPeriods), _) =>
          val spjRequestBtaVat = SpjRequestBtaVat(
            toAmountInPence(amount),
            appConfig.businessAccountHomeUrl,
            appConfig.businessAccountHomeUrl,
            VatPeriod(1, 2), //openPeriods.minBy(_.openPeriod),
            request.request.vatDecEnrolment.vrn.vrn)
          payConnector.vatPayLink(spjRequestBtaVat).map(response => Redirect(response.nextUrl))

        case _ => throw new Exception("")
      }

  }
}
