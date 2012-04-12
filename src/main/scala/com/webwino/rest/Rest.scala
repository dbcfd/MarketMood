package com.webwino.rest

import java.net.URLEncoder

import cc.spray._
import directives.{PathElement, Remaining}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import com.webwino.markit.{MarkitApi, LookupResult}
import com.webwino.models.Company
import com.webwino.actors._

trait Rest extends Directives {

  object resultCodes {
    val success = JInt(200)
    val failure = JInt(400)
    val noMatchesForSearch = JInt(401)
    val existingCompany = JInt(402)
  }

  def checkForCompanyNameLookup(s: String): Option[String] = {
    "^name=(.*)".r.unapplySeq(s) match {
      case Some(List(query)) => Some(query)
      case _ => None
    }
  }

  val restService = {
    path("test") {
      get {
        ctx => ({
          ctx.complete("Simple REST Service Test")
        })
      }
    } ~
      path("api" / "company") {
        get {
          ctx => ({
            //get all the companies currently in our database
            val companies = Company.getAllCompanies() map ((company: Company) => (company.symbol -> company.companyName))
            val resultDSL = (
              ("resultCode" -> resultCodes.success) ~
                ("companies" -> companies)
              )
            ctx.complete(compact(render(resultDSL)))
          })
        }
      } ~
      path("api" / "company" / PathElement / "SeriesData") {
        symbol =>
          parameters("start", "end") {
            (start, end) =>
              get {
                ctx => {
                  ctx.complete("Getting series data")
                }
              }
          }
      } ~
      path("api" / "company" / Remaining) {
        token =>
          get {
            ctx => {
              //check if we're doing a query
              checkForCompanyNameLookup(token) match {
                case Some(name) => getCompanyUsingName(name, ctx)
                case None => getCompanyUsingSymbol(token, ctx)
              }
            }
          } ~
          put {
            ctx => {
              //check if we're doing a query
              checkForCompanyNameLookup(token) match {
                case Some(name) => putCompanyUsingName(name, ctx)
                case None => putCompanyUsingSymbol(token, ctx)
              }
            }
          } ~
          delete {
            ctx => {
              deleteCompany(token, ctx)
            }
          }
      }
  }

  def deleteCompany(symbol: String, ctx: RequestContext) = {
    val companies = Company.queryBySymbol(symbol)
    for (company <- companies) Company.delete(company)
    val resultDSL = (
      ("resultCode" -> resultCodes.success) ~
        ("count" -> companies.length.toString())
      )
    ctx.complete(compact(render(resultDSL)))
  }

  def putCompanyUsingName(name: String, ctx: RequestContext) = {
    val companies = Company.queryByCompanyName(name)

    companies match {
      case Nil => {
        //No matches in our database, use the markit api for lookup, filtering matches by company name
        MarkitApi.lookup(name, (allSymbols: List[LookupResult]) => {
          val possibles = allSymbols filter (_.Name contains name)
          MarkitActor sendMessage (new RetrieveBySymbolList(possibles))
          val companiesBySymbol = possibles groupBy (_.Symbol) toList
          val uniqueCompanies = companiesBySymbol map (_._2.head)
          val sortedUniqueCompanies = uniqueCompanies sortWith ((e1, e2) => e1.Symbol < e2.Symbol)
          val companyList = sortedUniqueCompanies map ((res: LookupResult) => (res.Symbol -> res.Name))
          //create a dsl representing
          val resultDSL = (
            ("resultCode" -> resultCodes.success) ~
              ("companies" -> companyList)
            )
          ctx.complete(compact(render(resultDSL)))
        })
      }
      case _ => {
        val resultDSL = (
          ("resultCode" -> resultCodes.existingCompany)
          )
        ctx.complete(compact(render(resultDSL)))
      }
    }
  }

  def getCompanyUsingName(name: String, ctx: RequestContext) = {
    val companies = Company.queryByCompanyName(name)

    companies match {
      case Nil => {
        //create a dsl representing
        val resultDSL = (
          ("resultCode" -> resultCodes.noMatchesForSearch)
          )
        ctx.complete(compact(render(resultDSL)))
      }
      case _ => {
        val companyList = companies map ((company: Company) => (company.symbol -> company.companyName))
        val resultDSL = (
          ("resultCode" -> resultCodes.success) ~
            ("companies" -> companyList)
          )
        ctx.complete(compact(render(resultDSL)))
      }
    }
  }

  def putCompanyUsingSymbol(symbol: String, ctx: RequestContext) = {
    val companies = Company.queryBySymbol(symbol)

    companies match {
      case Nil => {
        ctx.request.content match {
          //see if the data was sent over to put a company manually
          case Some(content) => {
            val jsVal = parse(new String(content.buffer))
            val company = Company.toDb(jsVal)
            val resultDSL = (
              ("resultCode" -> resultCodes.success) ~
                ("companies" -> (
                  (company.symbol -> company.companyName)
                  )
                  )
              )
            ctx.complete(compact(render(resultDSL)))
          }
          case None => {
            //No matches in our database, use the markit api for lookup, filtering matches by company name
            MarkitApi.lookup(symbol, (allSymbols: List[LookupResult]) => {
              val possibles = allSymbols filter (_.Symbol contains symbol)
              MarkitActor sendMessage (new RetrieveBySymbolList(possibles))
              //use the Markit API for lookup
              val companiesBySymbol = possibles groupBy (_.Symbol) toList
              val uniqueCompanies = companiesBySymbol map (_._2.head)
              val sortedUniqueCompanies = uniqueCompanies sortWith ((e1, e2) => e1.Symbol < e2.Symbol)
              val companyList = sortedUniqueCompanies map ((res: LookupResult) => (res.Symbol -> res.Name))
              //create a dsl representing
              val resultDSL = (
                ("resultCode" -> resultCodes.success) ~
                  ("companies" -> companyList)
                )
              ctx.complete(compact(render(resultDSL)))
            })
          }
        }
      }
      case _ => {
        //create a dsl representing
        val resultDSL = (
          ("resultCode" -> resultCodes.existingCompany)
          )
        //dsl -> asl -> string
        ctx.complete(compact(render(resultDSL)))
      }
    }
  }

  def getCompanyUsingSymbol(symbol: String, ctx: RequestContext) = {
    val companies = Company.queryBySymbol(symbol)

    companies match {
      case Nil => {
        //create a dsl representing
        val resultDSL = (
          ("resultCode" -> resultCodes.noMatchesForSearch)
          )
        ctx.complete(compact(render(resultDSL)))
      }
      case _ => {
        val companyList = companies map ((company: Company) => (company.symbol -> company.companyName))
        //create a dsl representing
        val resultDSL = (
          ("resultCode" -> resultCodes.success) ~
            ("companies" -> companyList)
          )
        //dsl -> asl -> string
        ctx.complete(compact(render(resultDSL)))
      }
    }
  }
}