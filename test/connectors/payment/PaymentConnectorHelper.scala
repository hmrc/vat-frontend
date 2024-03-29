/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors.payment

import config.FrontendAppConfig
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait PaymentConnectorHelper extends AnyWordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite with Injecting {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit def ec: ExecutionContext = inject[ExecutionContext]

  def frontendAppConfig: FrontendAppConfig = inject[FrontendAppConfig]
}
