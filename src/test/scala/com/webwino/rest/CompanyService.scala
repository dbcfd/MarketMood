package com.webwino.rest

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._
import net.liftweb.json._
import cc.spray.typeconversion._
import org.specs2.matcher._
import com.webwino.rest.Company
import com.webwino.rest.Rest

class CompanyServiceSpec extends Specification with SprayTest with CompanyService {
  "The CompanyService" should {
    "return a success code response for GET requests to the api/companies path" in {
      testService(HttpRequest(GET, "/api/companies")) {
        restService
      }.response.content.as[String] must /("resultCode" -> ResultCodes.Success)
    }
  }
}