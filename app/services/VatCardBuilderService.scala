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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import controllers.actions.ServiceInfoAction
import controllers.helpers.AccountSummaryHelper
import javax.inject.Inject
import models.payment.{PaymentRecord, PaymentRecordFailure}
import connectors.models._
import controllers.actions.ServiceInfoAction
import controllers.helpers.AccountSummaryHelper
import models.payment.PaymentRecord
import models.requests.AuthenticatedRequest
import models.{Card, Link}
import play.api.i18n.{Messages, MessagesApi}
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.http.HeaderCarrier
import views.html.partials.vat.card.payments.payments_fragment_no_data
import views.html.partials.vat.card.returns.returns_fragment_no_data

import scala.concurrent.{ExecutionContext, Future}

class VatCardBuilderServiceImpl @Inject()(val messagesApi: MessagesApi,
                                          val vatPartialBuilder: VatPartialBuilder,
                                          serviceInfo: ServiceInfoAction,
                                          accountSummaryHelper: AccountSummaryHelper,
                                          appConfig: FrontendAppConfig,
                                          vatService: VatServiceInterface,
                                          paymentHistoryService: PaymentHistoryServiceInterface,
                                          linkProviderService: LinkProviderService
                                         )(implicit ec: ExecutionContext) extends VatCardBuilderService {

  def buildVatCard()(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier, messages: Messages): Future[Card] = {

    val paymentHistoryFuture = paymentHistoryService.getPayments(Some(request.vatDecEnrolment))
    val vatModelFuture = vatService.fetchVatModel(request.vatDecEnrolment)

    for {
      maybePaymentHistory <- paymentHistoryFuture
      vatAccountData <- vatModelFuture
      vatVarContent <- vatPartialBuilder.buildVatVarPartial(forCard = true).map { vatVarPartial => vatVarPartial.map(_.toString()) }
    } yield {
      vatAccountData match {
        case Right(None) => buildVatCardData(
          paymentsContent = Some(views.html.partials.vat.card.payments.payments_fragment_no_data().toString()),
          returnsContent = Some(views.html.partials.vat.card.returns.returns_fragment_no_data(appConfig, Some(request.vatDecEnrolment)).toString()),
          vatVarContent = vatVarContent,
          maybePaymentHistory,
          maybeLinksList = None
        )
        case Right(Some(data)) => buildVatCardData(
          paymentsContent = Some(vatPartialBuilder.buildPaymentsPartial(data).toString()),
          returnsContent = Some(vatPartialBuilder.buildReturnsPartial(data, request.vatDecEnrolment).toString()),
          vatVarContent = vatVarContent,
          maybePaymentHistory,
          linkProviderService.determinePaymentAdditionalLinks(data)
        )
        case _ => throw new Exception
      }
    }
  }

  private def buildVatCardData(paymentsContent: Option[String],
                               returnsContent: Option[String],
                               vatVarContent: Option[String],
                               maybePaymentHistory: Either[PaymentRecordFailure.type, List[PaymentRecord]],
                               maybeLinksList: Option[List[Link]]
                              )(implicit request: AuthenticatedRequest[_], messages: Messages, hc: HeaderCarrier): Card =
    Card(
      title = messagesApi.preferred(request)("partial.heading"),
      referenceNumber = request.vatDecEnrolment.vrn.value,
      primaryLink = Some(
        Link(
          href = appConfig.getUrl("mainPage"),
          ga = "link - click:VAT cards:More VAT details",
          id = "vat-account-details-card-link",
          title = messagesApi.preferred(request)("partial.heading")
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = paymentsContent,
      returnsPartial = returnsContent,
      vatVarPartial = vatVarContent,
      paymentHistory = maybePaymentHistory,
      paymentSectionAdditionalLinks = maybeLinksList
    )
}

@ImplementedBy(classOf[VatCardBuilderServiceImpl])
trait VatCardBuilderService {
  def buildVatCard()(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier, messages: Messages): Future[Card]
}
