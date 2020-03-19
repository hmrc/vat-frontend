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

package utils

import base.SpecBase

import scala.util.matching.Regex

class MessagesSpec extends SpecBase {

  private lazy val displayLine = "\n" + ("@" * 42) + "\n"

  private lazy val englishMesssages = messagesApi.messages("en")
  private lazy val welshMessages = messagesApi.messages("cy") -- messagesApi.messages("default").keys

  private def describeMismatch(defaultKeySet: Set[String], welshKeySet: Set[String]): String =
    if (defaultKeySet.size > welshKeySet.size)
      listMissingMessageKeys("The following message keys are missing from the Welsh Set:", defaultKeySet -- welshKeySet)
    else
      listMissingMessageKeys(
        "The following message keys are missing from the English Set:",
        welshKeySet -- defaultKeySet)

  private def listMissingMessageKeys(header: String, missingKeys: Set[String]) =
    missingKeys.toList.sorted.mkString(header + displayLine, "\n", displayLine)

  private def assertNonEmptyNonTemporaryValues(label: String, messages: Map[String, String]): Unit = messages.foreach {
    case (key: String, value: String) =>
      withClue(s"In $label, there is an empty value for the key:[$key][$value]") {
        value.trim.isEmpty mustBe false
      }
  }

  val MatchSingleQuoteOnly: Regex = """\w+'{1}\w+""".r
  val MatchBacktickQuoteOnly: Regex = """`+""".r

  private def assertCorrectUseOfQuotes(label: String, messages: Map[String, String]): Unit = messages.foreach {
    case (key: String, value: String) =>
      withClue(s"In $label, there is an unescaped or invalid quote:[$key][$value]") {
        MatchSingleQuoteOnly.findFirstIn(value).isDefined mustBe false
        MatchBacktickQuoteOnly.findFirstIn(value).isDefined mustBe false
      }
  }

  "The application" should {
    "have the correct message configs" in {
      messagesApi.messages.size mustBe 4
      messagesApi.messages.keys must contain theSameElementsAs Vector("en", "cy", "default", "default.play")
    }

    "have the same number of message keys in English and Welsh" in {
      englishMesssages.keySet.size mustBe welshMessages.keySet.size
    }

    "have the same keys in English and Welsh" in {
      withClue(describeMismatch(englishMesssages.keySet, welshMessages.keySet)) {
        englishMesssages.keySet mustBe welshMessages.keySet
      }
    }

    // 89% of app needs to be translated into Welsh. 90% allows for:
    //   - Messages which just can't be different from English
    //     E.g. addresses, acronyms, numbers, etc.
    //   - Content which is pending translation to Welsh
    "have different messages for English and Welsh" in {
      val same = englishMesssages.keys.collect({
        case key if englishMesssages.get(key) == welshMessages.get(key) =>
          (key, englishMesssages.get(key))
      })

      withClue("##############################################\n" +
        s"${same.size.toDouble * 100 / englishMesssages.size.toDouble}% of messages match between english and welsh\n$same\n") {
        same.size.toDouble / englishMesssages.size.toDouble < 0.11 mustBe true
      }
    }

    "have a non-empty message for each key" in {
      assertNonEmptyNonTemporaryValues("English", englishMesssages)
      assertNonEmptyNonTemporaryValues("Welsh", welshMessages)
    }

    "have no unescaped single quotes" in {
      assertCorrectUseOfQuotes("English", englishMesssages)
      assertCorrectUseOfQuotes("Welsh", welshMessages)
    }

    def isInteger(s: String): Boolean = s forall Character.isDigit

    def toArgArray(msg: String): Array[String] = msg.split("\\{|\\}").map(_.trim()).filter(isInteger)

    def countArgs(msg: String): Int = toArgArray(msg).length
    def listArgs(msg: String) = toArgArray(msg).mkString

    "have a resolvable message for keys which take arguments" in {
      val englishWithArgsMsgKeys = englishMesssages collect { case (key, value) if countArgs(value) > 0 => key }
      val welshWithArgsMsgKeys = welshMessages collect { case (key, value) if countArgs(value) > 0      => key }
      val missingFromEnglish = englishWithArgsMsgKeys.toList diff welshWithArgsMsgKeys.toList
      val missingFromWelsh = welshWithArgsMsgKeys.toList diff englishWithArgsMsgKeys.toList

      val clueTextForWelsh = missingFromWelsh.foldLeft("")((soFar, key) => s"$soFar$key has arguments in English but not in Welsh\n")
      val clueText = missingFromEnglish.foldLeft(clueTextForWelsh)((soFar, key) => s"$soFar$key has arguments in Welsh but not in English\n")

      withClue(clueText){
        englishWithArgsMsgKeys.size mustBe welshWithArgsMsgKeys.size
      }

    }

    "have the same args in the same order for all keys which take args" in {
      val englishWithArgsMsgKeysAndArgList = englishMesssages collect {
        case (key, value) if countArgs(value) > 0 => (key, listArgs(value))
      }
      val welshWithArgsMsgKeysAndArgList = welshMessages collect {
        case (key, value) if countArgs(value) > 0 => (key, listArgs(value))
      }
      val mismatchedArgSequences = englishWithArgsMsgKeysAndArgList collect {
        case (key, engArgSeq) if engArgSeq != welshWithArgsMsgKeysAndArgList(key) =>
          (key, engArgSeq, welshWithArgsMsgKeysAndArgList(key))
      }

      val mismatchedKeys = mismatchedArgSequences map (sequence => sequence._1)
      val clueText = mismatchedKeys.foldLeft("")((soFar, key) => s"$soFar$key does not have the same number of arguments in English and Welsh\n")
      withClue(clueText){
        mismatchedArgSequences.size mustBe 0
      }
    }
  }

}
