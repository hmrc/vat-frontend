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
import models.{Card, Link}
import play.api.i18n.Messages

import scala.concurrent.{ExecutionContext, Future}

trait VatCardBuilderService {
  //def buildVatCard(): Future[Card]
}

class VatCardBuilderServiceImpl @Inject() (val vatPartialBuilder: VatPartialBuilder)(implicit ec:ExecutionContext) extends VatCardBuilderService {

//  def buildVatCard()(implicit messages: Messages): Future[Card] = {
//    val cardData = buildCard(
//      paymentsContent = Some(vatPartialBuilder.buildPaymentsPartial().toString()),
//      returnsContent = Some(vatPartialBuilder.buildReturnsPartial.toString())
//    )
//    Future(cardData)
//  }


  private def buildCard(paymentsContent: Option[String] = None, returnsContent: Option[String] = None)(implicit messages: Messages): Card = {
    Card(
      title = messages("service.name.ir-sa"),
      description = "Some description",
      referenceNumber = "Some ref number",
      primaryLink = None,
      messageReferenceKey = Some("card.vat.vat_registration_number"),
      paymentsPartial = paymentsContent,
      returnsPartial = returnsContent
    )
  }

}
