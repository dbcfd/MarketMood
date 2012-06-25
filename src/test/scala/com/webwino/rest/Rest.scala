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
  "The RestService" should {
    "return a greeting for GET requests to the test path" in {
      testService(HttpRequest(GET, "/test")) {
        restService
      }.response.content.as[String] mustEqual Right("Simple REST Service Test")
    }

}