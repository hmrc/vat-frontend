/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.AuthAction._
import controllers.routes
import models.requests.AuthenticatedRequest
import models.{VatDecEnrolment, VatEnrolment, VatNoEnrolment, VatVarEnrolment, Vrn}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.AlternatePredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector,
                               config: FrontendAppConfig,
                               defaultParser: PlayBodyParsers)
                              (override implicit val executionContext: ExecutionContext) extends AuthAction with AuthorisedFunctions {


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


  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.withHeaders(request.headers), request.session)

    authorised(AlternatePredicate(AlternatePredicate(activatedEnrolment, notYetActivatedEnrolment), mtdEnrolment))
      .retrieve(Retrievals.externalId and Retrievals.allEnrolments and Retrievals.credentials) {
        case externalId ~ enrolments ~ Some(Credentials(credId, _)) =>
          externalId.map { externalId =>
            if (enrolments.getEnrolment(mtdEnrolmentKey).exists(mtdEnrolment => mtdEnrolment.isActivated)) {
              Future.successful(Redirect(config.getVatSummaryUrl("overview")))
            } else {
              block(AuthenticatedRequest(request, externalId, getVatDecEnrolment(enrolments), getVatVarEnrolment(enrolments), credId))
            }
        }.getOrElse(throw new UnauthorizedException("Unable to retrieve external Id"))
      case _ =>
        throw new UnauthorizedException("Unable to retrieve cred Id")
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: InsufficientEnrolments =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: InsufficientConfidenceLevel =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedAuthProvider =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedAffinityGroup =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedCredentialRole =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  override def parser: BodyParser[AnyContent] = defaultParser.defaultBodyParser
}

object AuthAction {
  val vatDecEnrolmentKey = "HMCE-VATDEC-ORG"
  val vatVarEnrolmentKey = "HMCE-VATVAR-ORG"
  val identifierKey = "VATRegNo"
  val mtdEnrolmentKey = "HMRC-MTD-VAT"
  val activatedEnrolment: Enrolment = Enrolment(vatDecEnrolmentKey)
  val notYetActivatedEnrolment: Enrolment = Enrolment(vatDecEnrolmentKey, Seq(), "NotYetActivated")
  val mtdEnrolment: Enrolment = Enrolment(mtdEnrolmentKey)
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]
