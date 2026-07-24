/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.views.Aliases.{Cy, En, UserResearchBanner}

class UrBannerUtilSpec extends SpecBase {

  "getUrBanner" when {

    "the showUserResearchBanner feature is disabled" should {

      "return None" in {
        val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
        when(appConfig.isUrBannerEnabled).thenReturn(false)
        UrBannerUtil.getUrBanner(appConfig)(messages) mustBe None
      }
    }

    "the showUserResearchBanner feature is enabled" when {
      val baseUrl = frontendAppConfig.urBannerBaseUrl

      "the language is English" should {

        "return a UserResearchBanner with En language and the English URL" in {
          UrBannerUtil.getUrBanner(frontendAppConfig)(messages) mustBe Some(UserResearchBanner(
            language = En,
            url = baseUrl,
            hideCloseButton = false
          ))
        }
      }

      "the language is Welsh" should {

        "return a UserResearchBanner with Cy language and the Welsh URL" in {
          val welshMessages = messagesApi.preferred(Seq(Lang("cy")))
          UrBannerUtil.getUrBanner(frontendAppConfig)(welshMessages) mustBe Some(UserResearchBanner(
            language = Cy,
            url = s"${baseUrl}&Q_Language=CY",
            hideCloseButton = false
          ))
        }
      }

      "hideCloseButton is true" should {

        "return a UserResearchBanner with hideCloseButton set to true" in {
          val result = UrBannerUtil.getUrBanner(frontendAppConfig, hideCloseButton = true)(messages)
          result.map(_.hideCloseButton) mustBe Some(true)
        }
      }
    }
  }
}