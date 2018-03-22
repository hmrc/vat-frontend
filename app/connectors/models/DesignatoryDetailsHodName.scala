/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors.models

import play.api.libs.json.{Json, OFormat}

case class DesignatoryDetailsHodName(traderName1: Option[String] = None,
                                     traderName2: Option[String] = None,
                                     traderName3: Option[String] = None
                                    )

object DesignatoryDetailsHodName {
  implicit val formats: OFormat[DesignatoryDetailsHodName] = Json.format[DesignatoryDetailsHodName]
}
