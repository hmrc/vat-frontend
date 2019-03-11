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

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatEnrolment, VatNoEnrolment, VatVarEnrolment}
import org.joda.time.DateTime
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.EmacUrlBuilder

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[VatVarPartialBuilderImpl])
trait VatVarPartialBuilder {
  def getPartialForCard()
                       (implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]]
  def getPartialForSubpage()
                          (implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]]
}

@Singleton
class VatVarPartialBuilderImpl @Inject()(val enrolmentsStore: EnrolmentsStoreService, emacUrlBuilder: EmacUrlBuilder, appConfig: FrontendAppConfig)
                                        (implicit val ec: ExecutionContext) extends  VatVarPartialBuilder{
  def getPartialForCard()
                       (implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier) : Future[Option[Html]] = {
    request.vatVarEnrolment match {
      case _:VatNoEnrolment => Future(Some(views.html.partials.account_summary.vat.vat_var.prompt_to_enrol_card(emacUrlBuilder, request.vatDecEnrolment)))
      case VatVarEnrolment(_, false) => {
        enrolmentsStore.showNewPinLink(request.vatVarEnrolment, DateTime.now).map{
          showPin => if(showPin){
            Some(
              views.html.partials.account_summary.vat.vat_var.vatvar_card_wrapper(
                views.html.partials.account_summary.vat.vat_var.prompt_to_activate_new_pin(
                  emacUrlBuilder, request.vatDecEnrolment, appConfig,appConfig.businessAccountHomeUrl, "link - click:Your business taxes cards:change your VAT details online",
                  "link - click:Your business taxes cards:Request a new vat var activation code"
                )
              )
            )
          } else {
            Some(
              views.html.partials.account_summary.vat.vat_var.vatvar_card_wrapper(
                views.html.partials.account_summary.vat.vat_var.prompt_to_activate_no_new_pin(
                  emacUrlBuilder, request.vatDecEnrolment, appConfig,appConfig.businessAccountHomeUrl, "link - click:Your business taxes cards:change your VAT details online"
                )
              )
            )
          }
        }
      }
      case _ => Future(None)
    }
  }
  def getPartialForSubpage()
                          (implicit request: AuthenticatedRequest[_], messages: Messages, headerCarrier: HeaderCarrier): Future[Option[Html]] ={

    request.vatVarEnrolment match {
      case _:VatNoEnrolment => Future(Some(views.html.partials.account_summary.vat.vat_var.vat_var_prompt_to_enrol(emacUrlBuilder, request.vatDecEnrolment)))
      case VatVarEnrolment(_, false) => {
        enrolmentsStore.showNewPinLink(request.vatVarEnrolment, DateTime.now).map{
          showPin => if(showPin){
            Some(
              views.html.partials.account_summary.vat.vat_var.vatvar_subpage_wrapper(
                views.html.partials.account_summary.vat.vat_var.prompt_to_activate_new_pin(
                  emacUrlBuilder, request.vatDecEnrolment, appConfig,request.uri, "link - click:VATVar:Enter pin","link - click:VATVar:Lost pin"
                )
              )
            )
          } else {
            Some(
              views.html.partials.account_summary.vat.vat_var.vatvar_subpage_wrapper(
                views.html.partials.account_summary.vat.vat_var.prompt_to_activate_no_new_pin(
                  emacUrlBuilder, request.vatDecEnrolment, appConfig,request.uri, "link - click:VATVar:Enter pin"
                )
              )
            )
          }
        }
      }
      case _ => Future(None)
    }
  }
}

