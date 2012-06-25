package com.webwino.rest

import cc.spray._
import directives.{PathElement, Remaining}

trait Rest extends Directives with AnalysisService with CompanyService {
  val restService = {
    path("test") {
      get {
        ctx => ({
          ctx.complete("Simple REST Service Test")
        })
      }
    } ~
      analysisRestService ~
      companyRestService
  }
}