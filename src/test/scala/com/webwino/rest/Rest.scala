package com.webwino.rest

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._
import net.liftweb.json._
import cc.spray.typeconversion._
import org.specs2.specification.{Given, When,  Then}
import org.specs2.matcher._
import scala.util.matching.Regex

class RestServiceSpec extends Specification with SprayTest with Rest with SprayJsonSupport {
  val codeResultParser = new Regex(""".*\"resultCode\"[.]*?:[.]*?(\d+).*""")

  "The RestService" should {
    "return a greeting for GET requests to the test path" in {
      testService(HttpRequest(GET, "/test")) {
        restService
      }.response.content.as[String] mustEqual Right("Simple REST Service Test")
    }
    "return a success code response for GET requests to the api/search patch" in {
      testService(HttpRequest(GET, "/api/search")) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.success)
    }
    "return a success code response and GM JsonObject for GET requests to the api/search/GM" in {
      testService(HttpRequest(GET, "/api/users/GM")) {
        restService
      }.response.content.as[String] must new JsonResultMatcher( (
        "resultCode" -> 200 ~
        "symbol" -> "GM" ~
        "exchanges" -> (
          "NYSE" ~
          "BATS"
          ) ~
        "description" -> "General Motors"
        ))
    }

    class JsonResultMatcher(val expJObj:JObject) extends Matcher[Either[DeserializationError, String]] {
      def apply[S <: Either[DeserializationError,String]](s: Expectable[S]) = { s.value match {
        case e:Right[_,_] => {
          val str:String = e.right.get
          val jObj:JObject = JsonParser.parse(str)
          val Diff(changed, added, deleted) = expJObj diff jObj
          changed match {
            case JNothing => {
              result(true,
                "Response " + str + " is " + expJObj.toString(),
                "Response " + str + " is not " + expJObj.toString(),
                s)
            }
            case _ => {
              result(false,
                "Response " + str + " is " + expJObj.toString(),
                "Response " + str + " is not " + expJObj.toString(),
                s)
            }
          }
        }
      } }
    }

  class CodeResultMatcher(val resultCode:JInt) extends Matcher[Either[DeserializationError, String]] {
    def apply[S <: Either[DeserializationError,String]](s: Expectable[S]) = { s.value match {
      case e:Right[_,_] => {
        val str:String = e.right.get
        str match {
          case codeResultParser(code) => {
            result(resultCode.toString().equals(code),
              "Response " + str + " with resultCode " + code + " is " + resultCode,
              "Response " + str + " with resultCode " + code + " is not " + resultCode,
              s)
          }
          case _ => {
            result(false,
              "Should never be displayed",
              "Response code not found: " + s.value.toString(),
              s)
          }
        }
      }
    }}
  }
}