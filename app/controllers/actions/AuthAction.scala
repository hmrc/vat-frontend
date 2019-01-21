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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import controllers.routes
import models.{VatDecEnrolment, VatEnrolment, VatNoEnrolment, VatVarEnrolment}
import models.requests.AuthenticatedRequest
import play.api.mvc.Results._
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.AlternatePredicate
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig)
                              (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  val vatDecEnrolmentKey = "HMCE-VATDEC-ORG"
  val vatVarEnrolmentKey = "HMCE-VATVAR-ORG"
  val identifierKey = "VATRegNo"
  val mtdEnrolmentKey = "HMRC-MTD-VAT"

  private[controllers] def getVatDecEnrolment(enrolments: Enrolments): VatDecEnrolment = {
    enrolments.getEnrolment(vatDecEnrolmentKey)
      .map(e => VatDecEnrolment(Vrn(e.getIdentifier(identifierKey).map(_.value)
        .getOrElse(throw new UnauthorizedException("Unable to retrieve VRN"))), e.isActivated))
          .getOrElse(throw new UnauthorizedException("unable to retrieve VAT enrolment"))
  }

  private[controllers] def getVatVarEnrolment(enrolments: Enrolments): VatEnrolment = {
    enrolments.getEnrolment(vatVarEnrolmentKey)
      .map(e => VatVarEnrolment(Vrn(e.getIdentifier(identifierKey).map(_.value)
        .getOrElse(throw new UnauthorizedException("Unable to retrieve VRN"))), e.isActivated))
          .getOrElse(VatNoEnrolment())
  }

  val activatedEnrolment = Enrolment(vatDecEnrolmentKey)
  val notYetActivatedEnrolment = Enrolment(vatDecEnrolmentKey, Seq(), "NotYetActivated")
  val mtdEnrolment = Enrolment(mtdEnrolmentKey)

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(AlternatePredicate(AlternatePredicate(activatedEnrolment, notYetActivatedEnrolment),mtdEnrolment)).retrieve(
      Retrievals.externalId and Retrievals.allEnrolments) {
      case externalId ~ enrolments =>
        externalId.map {
          externalId => if (enrolments.getEnrolment(mtdEnrolmentKey).exists(mtdEnrolment => mtdEnrolment.isActivated)) {
            Future(Redirect(config.getVatSummaryUrl("overview")))
          } else{
            block(AuthenticatedRequest(request, externalId, getVatDecEnrolment(enrolments), getVatVarEnrolment(enrolments)))
          }
        }.getOrElse(throw new UnauthorizedException("Unable to retrieve external Id"))
    } recover {
      case ex: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case ex: InsufficientEnrolments =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: InsufficientConfidenceLevel =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: UnsupportedAuthProvider =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: UnsupportedAffinityGroup =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: UnsupportedCredentialRole =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]
