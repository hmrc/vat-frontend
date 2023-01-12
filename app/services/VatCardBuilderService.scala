/*
 * Copyright 2023 HM Revenue & Customs
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
import models.payment.{PaymentRecord, PaymentRecordFailure}
import models.requests.AuthenticatedRequest
import models.{ActiveDirectDebit, Card, Link, VatData}
import play.api.i18n.{Messages, MessagesApi}
import services.payment.PaymentHistoryServiceInterface
import uk.gov.hmrc.http.HeaderCarrier
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatCardBuilderServiceImpl @Inject()(val messagesApi: MessagesApi,
                                          val vatPartialBuilder: VatPartialBuilder,
                                          appConfig: FrontendAppConfig,
                                          vatService: VatServiceInterface,
                                          paymentHistoryService: PaymentHistoryServiceInterface,
                                          linkProviderService: LinkProviderService)(implicit ec: ExecutionContext) extends VatCardBuilderService {

  def today: LocalDate = LocalDate.now()
  val deferralPeriodEndDate: LocalDate = LocalDate.of(2020,6,30)

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
          panelPartial = buildPanelInfo(None),
          paymentsContent = Some(views.html.partials.vat.card.payments.payments_fragment_no_data().toString()),
          returnsContent = Some(views.html.partials.vat.card.returns.returns_fragment_no_data(appConfig, Some(request.vatDecEnrolment)).toString()),
          vatVarContent = vatVarContent,
          maybePaymentHistory,
          maybeLinksList = None,
          accountBalance = None
        )
        case Right(optData@Some(data)) =>
          buildVatCardData(
            panelPartial = buildPanelInfo(optData),
            paymentsContent = Some(vatPartialBuilder.buildPaymentsPartial(data).toString()),
            returnsContent = Some(vatPartialBuilder.buildReturnsPartial(data, request.vatDecEnrolment).toString()),
            vatVarContent = vatVarContent,
            maybePaymentHistory,
            linkProviderService.determinePaymentAdditionalLinks(data),
            accountBalance = data.accountSummary.accountBalance.flatMap(_.amount)
          )
        case _ => throw new Exception
      }
    }
  }

  private def buildPanelInfo(optData: Option[VatData])(
    implicit messages: Messages
  ): Option[String] = {
    val optHasDirectDebit: Option[Boolean] =
      for {
        data <- optData
        calendar <- data.calendar
        directDebit = calendar.directDebit
      } yield directDebit match {
        case ActiveDirectDebit(_) => true
        case _ => false
      }
    Some(views.html.partials.vat.card.panel_info(optHasDirectDebit, appConfig, today.isAfter(deferralPeriodEndDate)).toString())
  }

  private def buildVatCardData(panelPartial: Option[String],
                               paymentsContent: Option[String],
                               returnsContent: Option[String],
                               vatVarContent: Option[String],
                               maybePaymentHistory: Either[PaymentRecordFailure.type, List[PaymentRecord]],
                               maybeLinksList: Option[List[Link]],
                               accountBalance: Option[BigDecimal])
                              (implicit request: AuthenticatedRequest[_]): Card = {
    Card(
      title = messagesApi.preferred(request)("partial.heading"),
      referenceNumber = request.vatDecEnrolment.vrn.value,
      primaryLink = Some(
        Link(
          href = appConfig.getUrl("mainPage"),
          id = "vat-account-details-card-link",
          title = messagesApi.preferred(request)("partial.heading")
        )
      ),
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      panelPartial = panelPartial,
      paymentsPartial = paymentsContent,
      returnsPartial = returnsContent,
      vatVarPartial = vatVarContent,
      paymentHistory = maybePaymentHistory,
      paymentSectionAdditionalLinks = maybeLinksList,
      accountBalance = accountBalance
    )
  }
}

@ImplementedBy(classOf[VatCardBuilderServiceImpl])
trait VatCardBuilderService {
  def buildVatCard()(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier, messages: Messages): Future[Card]
}
