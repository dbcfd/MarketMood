package com.webwino.rest

import java.net.URLEncoder

import cc.spray._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import directives.Remaining

import com.webwino.markit.{MarkitApi, LookupResult}
import com.webwino.models.Company
import com.webwino.actors._

trait Rest extends Directives {
  object resultCodes {
    val success = JInt(200)
    val failure = JInt(400)
  }
  
  val restService = {
    path("test") {
      get { _.complete("Simple REST Service Test") }
    } ~
    path ("api" / "search") {
      get { ctx => ( {
        //create a dsl representing
        val companies = Company.getAllCompanies() map ( (company:Company) => ( company.symbol -> company.companyName) )
        val resultDSL = (
          ("resultCode" -> resultCodes.success) ~
          ("companies" -> companies)
          )
        ctx.complete(compact(render(resultDSL)))
      } ) }
    } ~
      path ("api" / "search" / "symbol" / Remaining) { token =>
        get { ctx => ( {
          val companies = Company.queryBySymbol(token)
          
          val companyList = companies match {
            case Nil => {
              //No matches in our database, use the markit api for lookup, filtering matches by company name
              val possibles = MarkitApi.lookup(token) filter ( _.Symbol contains token)
              MarkitActor sendMessage (new RetrieveBySymbolList(possibles))
              //use the Markit API for lookup
              val companiesBySymbol = possibles groupBy (_.Symbol) toList
              val uniqueCompanies = companiesBySymbol map (_._2.head)
              val sortedUniqueCompanies = uniqueCompanies sortWith ( (e1,e2) => e1.Symbol < e2.Symbol)
              sortedUniqueCompanies map ( (res:LookupResult) => (res.Symbol -> res.Name) )
            }
            case _ => companies map ( (company:Company) => ( company.symbol -> company.companyName))
          }

          //create a dsl representing
          val resultDSL = (
            ("resultCode" -> resultCodes.success) ~
            ("companies" -> companyList)
          )
          //dsl -> asl -> string
          ctx.complete(compact(render(resultDSL)))
        } ) }
      } ~
      path ("api" / "search" / "company" / Remaining) { token =>
        get { ctx => ( {
          val companies = Company.queryByCompanyName(token)

          val companyList = companies match {
            case Nil => {
              //No matches in our database, use the markit api for lookup, filtering matches by company name
              val possibles = MarkitApi.lookup(token) filter ( _.Name contains token)
              MarkitActor sendMessage (new RetrieveBySymbolList(possibles))
              val companiesBySymbol = possibles groupBy (_.Symbol) toList
              val uniqueCompanies = companiesBySymbol map (_._2.head)
              val sortedUniqueCompanies = uniqueCompanies sortWith ( (e1,e2) => e1.Symbol < e2.Symbol)
              sortedUniqueCompanies map ( (res:LookupResult) => (res.Symbol -> res.Name) )
            }
            case _ => companies map ( (company:Company) => ( company.symbol -> company.companyName))
          }

          //create a dsl representing
          val resultDSL = (
            ("resultCode" -> resultCodes.success) ~
              ("companies" -> companyList)
            )
          ctx.complete(compact(render(resultDSL)))
        } ) }
      }
  }
}