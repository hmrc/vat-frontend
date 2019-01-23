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

package connectors.models.designatorydetails

import play.api.libs.json.{Json, OFormat}

case class DesignatoryDetailsAddress(addressLine1: Option[String] = None,
                                     addressLine2: Option[String] = None,
                                     addressLine3: Option[String] = None,
                                     addressLine4: Option[String] = None,
                                     addressLine5: Option[String] = None,
                                     postcode: Option[String] = None,
                                     foreignCountry: Option[String] = None
                              )

object DesignatoryDetailsAddress {
  implicit val formats: OFormat[DesignatoryDetailsAddress] = Json.format[DesignatoryDetailsAddress]
}
