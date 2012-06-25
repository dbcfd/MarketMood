package com.webwino.rest

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._
import com.webwino.rest.Rest

class AnalysisServiceSpec extends Specification with SprayTest with AnalysisService {

  "The AnalysisService" should {
    "return a success code for GET requests to api/companies/:symbol/analysis with valid symbol" in {
      testService(HttpRequest(GET, "/api/companies/TEST/analysis")) {
        restService
      }.response.content.as[String] must /("resultCodes" -> ResultCodes.Success)
    }
  }
}