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

package services

import config.FrontendAppConfig
import models.VatThreshold
import play.api.mvc.Request
import uk.gov.hmrc.http.InternalServerException
import utils.LoggingUtil
import utils.CurrencyFormatter

import java.time._
import javax.inject.{Inject, Singleton}


@Singleton
class ThresholdService @Inject()()(implicit appConfig: FrontendAppConfig) extends LoggingUtil {

  def now: LocalDateTime = LocalDateTime.now()

  def getVatThreshold: Option[VatThreshold] = {
    logger.debug(s"[ThresholdService][getVatThreshold] thresholds from config \n ${appConfig.thresholds}")
    appConfig.thresholds
      .sortWith(_.dateTime isAfter _.dateTime)
      .find(model => now.isAfter(model.dateTime) || now.isEqual(model.dateTime))
  }

  def formattedVatThreshold()(implicit request: Request[_]): String = {
    getVatThreshold.fold{
      val msg = "[ThresholdService][formattedThreshold] Could not retrieve threshold from config"
      errorLog(msg)
      throw new InternalServerException(msg)
    }{ threshold =>
      infoLog(s"[ThresholdService][formattedThreshold] displayed threshold ${threshold.amount}")
      CurrencyFormatter.formatCurrency(threshold.amount)
    }
  }
}