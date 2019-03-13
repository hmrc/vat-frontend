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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import connectors.models._
import controllers.actions.{AuthAction, ServiceInfoAction}
import controllers.helpers.AccountSummaryHelper
import javax.inject.Inject
import models.payment.PaymentRecord
import models.requests.AuthenticatedRequest
import models.{Card, Link}
import org.joda.time.LocalDate
import play.api.i18n.{Messages, MessagesApi}
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class VatCardBuilderServiceImpl @Inject()(val messagesApi: MessagesApi,
                                          val vatPartialBuilder: VatPartialBuilder,
                                          serviceInfo: ServiceInfoAction,
                                          accountSummaryHelper: AccountSummaryHelper,
                                          appConfig: FrontendAppConfig,
                                          vatService: VatServiceInterface,
                                          paymentHistoryService: PaymentHistoryServiceInterface
                                         )(implicit ec: ExecutionContext) extends VatCardBuilderService {

  def buildVatCard()(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier, messages: Messages): Future[Card] = {

    val paymentHistoryFuture = paymentHistoryService.getPayments(Some(request.vatDecEnrolment))
    val vatModelFuture = vatService.fetchVatModel(Some(request.vatDecEnrolment))

    val data = for {
      paymentHistory <- paymentHistoryFuture
      vatAccountData <- vatModelFuture
    } yield {
      (paymentHistory, vatAccountData)
    }

    data.flatMap { x =>

      x._2 match {
        case VatGenericError => ???
        case VatNoData => buildVatCardData(
          paymentsContent = Some(views.html.partials.vat.card.payments.payments_fragment_no_data().toString()),
          returnsContent = Some(""),
          vatVarContent = vatPartialBuilder.buildVatVarPartial(forCard = true).map { vatVarPartial => vatVarPartial.map(_.toString()) },
          x._1
        )
        case VatEmpty => ???
        case VatUnactivated => ???
        case data: VatData => buildVatCardData(
          paymentsContent = Some(vatPartialBuilder.buildPaymentsPartial(data).toString()),
          returnsContent = Some(vatPartialBuilder.buildReturnsPartial(data, request.vatDecEnrolment).toString()),
          vatVarContent = vatPartialBuilder.buildVatVarPartial(forCard = true).map { vatVarPartial => vatVarPartial.map(_.toString()) },
          x._1
        )
      }
    }

  }

  def buildVatCardData(paymentsContent: Option[String] = None,
                       returnsContent: Option[String] = None,
                       vatVarContent: Future[Option[String]] = Future(Some("")),
                       payments: List[PaymentRecord]
                      )(implicit request: AuthenticatedRequest[_], messages: Messages, hc: HeaderCarrier): Future[Card] = {
    vatVarContent.map { x =>
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
        vatVarPartial = x,
        paymentHistory = payments
      )
    }
  }
}

@ImplementedBy(classOf[VatCardBuilderServiceImpl])
trait VatCardBuilderService {
  def buildVatCard()(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier, messages: Messages): Future[Card]
}
