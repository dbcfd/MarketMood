package com.webwino.rest

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._

class CompanyServiceSpec extends Specification with SprayTest with CompanyService {
  "The CompanyService" should {
    "return a success code response for GET requests to the api/companies path" in {
      testService(HttpRequest(GET, "/api/companies")) {
        companyRestService
      }.response.content.as[String] must beRight.like {case str => str must /("resultCode" -> ResultCodes.success)}
    }
  }
}