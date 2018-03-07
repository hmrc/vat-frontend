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

package metrics

import com.codahale.metrics.MetricRegistry
// TODO: replaced "import uk.gov.hmrc.play.graphite.MicroserviceMetrics" with this -->
import uk.gov.hmrc.play.bootstrap.graphite.GraphiteMetricsModule

trait Metrics extends GraphiteMetricsModule {
  def metricsRegistry: MetricRegistry

  val saPartialForbiddenAgent = metricsRegistry.meter("saPartial.forbiddenAgent")
  val saPartialForbiddenNoSaGovernmentGatewayEnrolment = metricsRegistry.meter("saPartial.forbiddenNoSaGovernmentGatewayEnrolment")
  val saPartialForbiddenNoSaAccountOnAuthContext = metricsRegistry.meter("saPartial.forbiddenNoSaAccountOnAuthContext")
  val saPartialNotYetActivated = metricsRegistry.meter("saPartial.notYetActivated")
  val saPartialActivated = metricsRegistry.meter("saPartial.activated")
  val authenticatedWithout2SV = metricsRegistry.meter("authentication.two-step-verification.false")
  val authenticatedWith2SV = metricsRegistry.meter("authentication.two-step-verification.true")
  val vatSummaryAndCalendarMatch = metricsRegistry.meter("vat.hods.summary-and-calendar.match.true")
  val vatSummaryAndCalendarMismatch = metricsRegistry.meter("vat.hods.summary-and-calendar.match.false")
  val vatSummaryAndCalendarSummaryEmpty = metricsRegistry.meter("vat.hods.summary-and-calendar.summary.empty")
  val vatSummaryAndCalendarCalendarEmpty = metricsRegistry.meter("vat.hods.summary-and-calendar.calendar.empty")
  val vatSummaryAndCalendarBothEmpty = metricsRegistry.meter("vat.hods.summary-and-calendar.both.empty")
  val vatSummaryAndCalendarError = metricsRegistry.meter("vat.hods.summary-and-calendar.match.error")

  def markSaPartialForbiddenAgent() = saPartialForbiddenAgent.mark()
  def markSaPartialForbiddenNoSaGovernmentGatewayEnrolment() = saPartialForbiddenNoSaGovernmentGatewayEnrolment.mark()
  def markSaPartialForbiddenNoSaAccountOnAuthContext() = saPartialForbiddenNoSaAccountOnAuthContext.mark()
  def markSaPartialNotYetActivated() = saPartialNotYetActivated.mark()
  def markAuthenticatedWithout2SV() = authenticatedWithout2SV.mark()
  def markAuthenticatedWith2SV() = authenticatedWith2SV.mark()
  def markSaPartialActivated() = saPartialActivated.mark()
  def markVatSummaryAndCalendarMatch() = vatSummaryAndCalendarMatch.mark()
  def markVatSummaryAndCalendarMismatch() = vatSummaryAndCalendarMismatch.mark()
  def markVatSummaryAndCalendarSummaryEmpty() = vatSummaryAndCalendarSummaryEmpty.mark()
  def markVatSummaryAndCalendarCalendarEmpty() = vatSummaryAndCalendarCalendarEmpty.mark()
  def markVatSummaryAndCalendarBothEmpty() = vatSummaryAndCalendarBothEmpty.mark()
  def markVatSummaryAndCalendarError() = vatSummaryAndCalendarError.mark()
}

object Metrics extends Metrics {
  override def metricsRegistry: MetricRegistry = com.codahale.metrics.SharedMetricRegistries.getOrCreate("default")
}
