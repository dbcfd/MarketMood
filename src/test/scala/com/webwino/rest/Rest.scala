package com.webwino.rest

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._
import net.liftweb.json._
import cc.spray.typeconversion._
import net.liftweb.json.JsonDSL._
import org.specs2.matcher._
import scala.util.matching.Regex

class RestServiceSpec extends Specification with SprayTest with Rest {
  val codeResultParser = new Regex(""".*\"resultCode\"[.]*?:[.]*?(\d+).*""")
  val company = ("symbol" -> "SOMEBADSYMBOL") ~
    ("companyName" -> "a test company") ~
    ("exchanges" -> (
      "NYSE",
      "NASDAQ"
      ))

  "The RestService" should {
    "return a greeting for GET requests to the test path" in {
      testService(HttpRequest(GET, "/test")) {
        restService
      }.response.content.as[String] mustEqual Right("Simple REST Service Test")
    }
    "return a success code response for GET requests to the api/search path" in {
      testService(HttpRequest(GET, "/api/company")) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.success)
    }
    "return a failure code response for GET requests to the api/company/SOMEBADSYMBOL path" in {
      testService(HttpRequest(GET, "/api/company/SOMEBADSYMBOL")) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.failure)
    }
    "return a success code response for PUT requests to the api/company/SOMEBADSYMBOL path" in {
      testService(HttpRequest(PUT, "/api/company/SOMEBADSYMBOL", Nil, Some(HttpContent(compact(render(company)))))) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.success)
    }
    "return a success code response for GET requests to the api/company/SOMEBADSYMBOL path" in {
      testService(HttpRequest(GET, "/api/company/SOMEBADSYMBOL")) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.success)
    }
    "return a success code response for GET requests to the api/company/name=test path" in {
      testService(HttpRequest(GET, "/api/company/name=test")) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.success)
    }
    "return a failure code response for DELETE requests to the api/company/SOMEBADSYMBOL path" in {
      testService(HttpRequest(DELETE, "/api/company/SOMEBADSYMBOL")) {
        restService
      }.response.content.as[String] must new CodeResultMatcher(resultCodes.success)
    }
  }

  class JsonResultMatcher(val expJObj: JObject) extends Matcher[Either[DeserializationError, String]] {
    def apply[S <: Either[DeserializationError, String]](s: Expectable[S]) = {
      s.value match {
        case e: Right[_, _] => {
          val str: String = e.right.get
          val jObj: JValue = JsonParser.parse(str)
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
      }
    }
  }

  class CodeResultMatcher(val resultCode: JInt) extends Matcher[Either[DeserializationError, String]] {
    implicit val formats = DefaultFormats

    def apply[S <: Either[DeserializationError, String]](s: Expectable[S]) = {
      s.value match {
        case e: Right[_, _] => {
          val str: String = e.right.get
          str match {
            case codeResultParser(code) => {
              result(resultCode.extract[String] equals code,
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
      }
    }
  }

}