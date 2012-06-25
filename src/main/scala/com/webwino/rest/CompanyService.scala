package com.webwino.rest

import cc.spray._
import directives.{PathElement, Remaining}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.webwino.models.Company

trait CompanyService extends Directives {
  val companyRestService = path("api" / "companies") {
    get {
      ctx => ({
        //get all the companies currently in our database
        val companies = Company.getAllCompanies()
        val resultDSL = (
          ("resultCode" -> ResultCodes.success) ~
            ("companies" -> companies)
          )
        ctx.complete(compact(render(resultDSL)))
      })
    } ~
      post {
        ctx => ({
          val resultDSL = (
            ("resultCode" -> ResultCodes.success) ~
              ("company" -> "some company")
            )
          ctx.complete(compact(render(resultDSL)))
        })
      } ~
      path("api" / "company" / Remaining) {
        (symbol: String) =>
          get {
            ctx => {
              getCompanyUsingSymbol(symbol, ctx)
            }
          } ~
            post {
              ctx => {
                postCompanyUsingSymbol(symbol, ctx)
              }
            } ~
            delete {
              ctx => {
                deleteCompany(symbol, ctx)
              }
            }
      }
  }

  def getCompanyUsingSymbol(symbol: String, ctx: RequestContext) = {
    val companies = Company.queryBySymbol(symbol)
    if (companies isEmpty) {
      val resultDSL = (
        ("resultCode" -> ResultCodes.failure) ~
          ("errorMessage" -> ("No companies exist for symbol " + symbol))
        )
      ctx.complete(compact(render(resultDSL)))
    }
    else {
      //for(company <- companies) Company.update(company)
      val resultDSL = (
        ("resultCode" -> ResultCodes.success) ~
          ("count" -> companies.size)
        )
      ctx.complete(compact(render(resultDSL)))
    }
  }

  def deleteCompany(symbol: String, ctx: RequestContext) = {
    val companies = Company.queryBySymbol(symbol)
    for (company <- companies) Company.delete(company)
    val resultDSL = (
      ("resultCode" -> ResultCodes.success) ~
        ("count" -> companies.length.toString())
      )
    ctx.complete(compact(render(resultDSL)))
  }

  def postCompanyUsingSymbol(symbol: String, ctx: RequestContext) = {
    ctx.request.content match {
      case Some(companyDate) => {
        val companies = Company.queryBySymbol(symbol)
        if (companies isEmpty) {
          val resultDSL = (
            ("resultCode" -> ResultCodes.failure) ~
              ("errorMessage" -> ("No companies exist for symbol " + symbol))
            )
          ctx.complete(compact(render(resultDSL)))
        }
        else {
          //for(company <- companies) Company.update(company)
          val resultDSL = (
            ("resultCode" -> ResultCodes.success) ~
              ("count" -> companies.size)
            )
          ctx.complete(compact(render(resultDSL)))
        }
      }
      case _ => {
        val resultDSL = (
          ("resultCode" -> ResultCodes.failure) ~
            ("errorMessage" -> "JSON content required to update companies")
          )
        ctx.complete(compact(render(resultDSL)))
      }
    }
  }
}