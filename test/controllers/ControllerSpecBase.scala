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

package controllers

import base.SpecBase
import controllers.actions.{AuthAction, FakeServiceInfoAction, ServiceInfoAction}
import controllers.actions.mocks.MockAuth
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.guice.GuiceableModule.fromPlayBinding
import play.api.inject.{Binding, bind}

trait ControllerSpecBase extends SpecBase with MockAuth {

  def moduleOverrides: Seq[Binding[_]] = Seq.empty

  private def commonOverrides: Seq[Binding[_]] = Seq[Binding[_]](
    bind[AuthAction].toInstance(mockAuthAction),
    bind[ServiceInfoAction].toInstance(FakeServiceInfoAction)
  )

  private def allOverrides: Seq[Binding[_]] = commonOverrides ++ moduleOverrides

  final override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(allOverrides.map(fromPlayBinding): _*)
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAuth()
  }

}
