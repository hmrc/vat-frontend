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
import connectors.models.{AccountBalance, AccountSummaryData, VatData, VatNoData}
import controllers.actions._
import controllers.helpers.AccountSummaryHelper
import javax.inject.Inject
import models.{Card, Link}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent}
import models.requests.AuthenticatedRequest
import services.{VatCardBuilderService, VatPartialBuilder, VatServiceInterface}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.partial

import scala.concurrent.ExecutionContext.Implicits.global


class PartialController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  authenticate: AuthAction,
                                  serviceInfo: ServiceInfoAction,
                                  accountSummaryHelper: AccountSummaryHelper,
                                  appConfig: FrontendAppConfig,
                                  vatService: VatServiceInterface,
                                  vatPartialBuilder: VatPartialBuilder,
                                  vatCardBuilderService: VatCardBuilderService
                                 ) extends FrontendController with I18nSupport {

  def onPageLoad = authenticate.async {
    implicit request =>
      vatService.fetchVatModel(Some(request.vatDecEnrolment)).map(
        vatModel => {
          val accountView = accountSummaryHelper.getAccountSummaryView(vatModel, showCreditCardMessage = false)
          Ok(partial(request.vatDecEnrolment.vrn, appConfig, accountView))
        }
      )
  }

  def getCard: Action[AnyContent] = authenticate.async { implicit request =>
    vatCardBuilderService.buildVatCard().map(card => Ok(toJson(card)))
  }



  // TODO: remove
  def getCardOriginal: Action[AnyContent] = authenticate.async { implicit request =>
    vatService.fetchVatModel(Some(request.vatDecEnrolment)).map {
      case data: VatData => Ok(toJson(
         Card(
           title = messagesApi.preferred(request)("partial.heading"),
           description = getBalanceMessage(data),
           referenceNumber = request.vatDecEnrolment.vrn.value,
           primaryLink = Some(
             Link(
               href = appConfig.getUrl("mainPage"),
               ga = "link - click:Your business taxes cards:More VAT details",
               id = "vat-account-details-card-link",
               title = messagesApi.preferred(request)("partial.heading")
             )
           ),
           messageReferenceKey = Some("card.vat.vat_registration_number"),
           //paymentsPartial = Some(vatPartialBuilder.buildPaymentsPartial(data.accountSummary).toString()),
           paymentsPartial = Some(vatPartialBuilder.buildPaymentsPartialNew(data).toString()),
           returnsPartial = Some("<p> Returns - WORK IN PROGRESS</p>")
         )
      ))
      case _             => println("\n\nVAT prototype scenario 7") // FIXME
                            InternalServerError("Failed to get VAT data from the backend")
    } recover {
      case _             => InternalServerError("Failed to get data from the backend")
    }
  }


  // TODO: remove
  def getCardNewAAA: Action[AnyContent] = authenticate.async { implicit request =>
    vatService.fetchVatModel(Some(request.vatDecEnrolment)).map { vatAccountData =>
      vatAccountData match {
        case data: VatData => Ok(toJson(
                                Card(
                                  title = messagesApi.preferred(request)("partial.heading"),
                                  description = getBalanceMessage(data),
                                  referenceNumber = request.vatDecEnrolment.vrn.value,
                                  primaryLink = Some(
                                    Link(
                                      href = appConfig.getUrl("mainPage"),
                                      ga = "link - click:Your business taxes cards:More VAT details",
                                      id = "vat-account-details-card-link",
                                      title = messagesApi.preferred(request)("partial.heading")
                                    )
                                  ),
                                  //paymentsPartial = Some(vatPartialBuilder.buildPaymentsPartial(data.accountSummary).toString()),
                                  paymentsPartial = Some(vatPartialBuilder.buildPaymentsPartialNew(data).toString()),
                                  returnsPartial = Some("<p>Returns - WORK IN PROGRESS</p>")
                                )
                              ))
        case VatNoData => Ok(toJson(
                            Card(
                              title = messagesApi.preferred(request)("partial.heading"),
                              description = "blabla",
                              referenceNumber = request.vatDecEnrolment.vrn.value,
                              primaryLink = Some(
                                Link(
                                  href = appConfig.getUrl("mainPage"),
                                  ga = "link - click:Your business taxes cards:More VAT details",
                                  id = "vat-account-details-card-link",
                                  title = messagesApi.preferred(request)("partial.heading")
                                )
                              ),
                              paymentsPartial = Some("BLA BLA BLA BLA BLA BLA BLA BLA BLA BLA BLA"),
                              returnsPartial = Some("<p>Returns - WORK IN PROGRESS</p>")
                            )
                          ))
        case _             => InternalServerError("Failed to get VAT data from the backend")
      }
    } recover {
      case _             => InternalServerError("Failed to get data from the backend")
    }
  }

  // TODO: do we need this?
  private def getBalanceMessage(data: VatData)(implicit request: AuthenticatedRequest[AnyContent]): String = {
    data.accountSummary match {
      case AccountSummaryData(Some(AccountBalance(Some(amount))), _, _) => {
        if (amount < 0) {
          messagesApi.preferred(request)("account.in.credaait", f"£${amount.abs}%.2f")
        } else if (amount > 0) {
          messagesApi.preferred(request)("account.aadue", f"£${amount.abs}%.2f")
        } else {
          messagesApi.preferred(request)("account.nothing.to.payaa")
        }
      }
      case _ => ""
    }
  }

}
