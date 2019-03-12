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

package connectors.payment

import config.FrontendAppConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

trait PaymentConnectorHelper extends UnitSpec with ScalaFutures with GuiceOneAppPerSuite {
  implicit val hc = HeaderCarrier()

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

//  override lazy val fakeApplication = FakeApplication(additionalConfiguration = Map(
//    "govuk-tax.Test.services.pay-api.host" -> "localhost",
//    "govuk-tax.Test.services.pay-api.port" -> 9057))

//  val spjRequest = SpjRequestBtaSa("1234567890", 200l, "/", "/")
//  val payUrl = NextUrl("localhost:9057/pay")
//  val errorUrl = NextUrl("http://localhost:6002/pay-online/service-unavailable")
}