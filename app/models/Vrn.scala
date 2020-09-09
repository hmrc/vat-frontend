/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import play.api.libs.json._

import scala.util.{Failure, Success, Try}


case class Vrn(vrn: String) extends TaxIdentifier with SimpleName {
  override def toString = vrn
  val name = "vrn"
  def value = vrn
}

object Vrn extends (String => Vrn) {
  implicit val vrnWrite: Writes[Vrn] = new SimpleObjectWrites[Vrn](_.value)
  implicit val vrnRead: Reads[Vrn] = new SimpleObjectReads[Vrn]("vrn", Vrn.apply)
}

trait TaxIdentifier {
  def value: String
}

trait SimpleName {
  val name: String
}

class SimpleObjectWrites[T](val valueGetter: T => String) extends Writes[T] {
  override def writes(value: T): JsValue = JsString(valueGetter(value))
}

class SimpleObjectReads[T](val fieldName: String, val constructor: String => T) extends Reads[T] {
  override def reads(js: JsValue): JsResult[T] = Try {
    js match {

      case v: JsString => v.validate[String].map(constructor)
      case v: JsObject => (v \ fieldName).validate[String].map(constructor)
      case noParsed => JsError(s"Could not read Json value of $fieldName in $noParsed")
    }
  } match {
    case Success(jsResult) => jsResult
    case Failure(exception) => JsError(exception.getMessage)
  }
}