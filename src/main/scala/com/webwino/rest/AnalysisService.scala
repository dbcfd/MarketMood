package com.webwino.rest

import cc.spray._
import directives.{PathElement, Remaining}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

trait AnalysisService extends Directives {
  val analysisRestService = path("api" / "companies" / PathElement / "analysis") {
    symbol: String =>
      get {
        ctx => {
          val resultDSL = (
            ("resultCode" -> ResultCodes.success) ~
              ("analysis" -> "Current analysis")
            )
          ctx.complete(compact(render(resultDSL)))
        }
      } ~
        post {
          ctx => {
            val resultDSL = (
              ("resultCode" -> ResultCodes.success) ~
                ("analysis" -> "add new analysis")
              )
            ctx.complete(compact(render(resultDSL)))
          }
        } ~
        put {
          ctx => {
            val resultDSL = (
              ("resultCode" -> ResultCodes.success) ~
                ("analysis" -> "update analysis")
              )
            ctx.complete(compact(render(resultDSL)))
          }
        } ~
        delete {
          ctx => {
            val resultDSL = (
              ("resultCode" -> ResultCodes.success) ~
                ("analysis" -> "delete analysis")
              )
            ctx.complete(compact(render(resultDSL)))
          }
        }
  }
}