package com.webwino.rest

import java.net.URLEncoder

import cc.spray._
import net.liftweb.json._
import directives.Remaining
import cc.spray.utils.Logging

import com.webwino.markit.MarkitApi
import com.webwino.models.Company

trait Rest extends Directives with Logging {
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
        log.debug("Performing symbol list")
        
        //create a dsl representing 
        val resultDSL = (
          "resultCode" -> resultCodes.success ~
            "companies" -> Company.getAllCompanies() map ( (company:Company) => ( company.symbol -> company.companyName) )
          )
        ctx.complete(render(resultDSL))
      } ) }
    } ~
      path ("api" / "search" / "symbol" / Remaining) { token =>
        get { ctx => ( {
          log.debug("Performing symbol lookup")
          
          val companies = Company.queryBySymbol(token)
          
          val companyList = companies match {
            case Nil => {
              //No matches in our database, use the markit api for lookup, filtering matches by company name
              val possibles = MarkitApi.lookup(token) filter ( _.symbol contains token)
              MarkitActor ! RetrieveBySymbolList(possibles)
              //use the Markit API for lookup
              val sortedUniqueCompanies = possibles groupBy(_.symbol) map(_._2.head) sort( (e1,e2) => (e1.symbol < e2.symbol))
              sortedUniqueCompanies map ( (company:Company) => (company.symbol -> company.companyName))
            }
            case _ => companies map ( (company:Company) => ( company.symbol -> company.companyName))
          }

          //create a dsl representing
          val resultDSL = (
            "resultCode" -> resultCodes.success ~
              "companies" -> companyList
            )
          ctx.complete(render(resultDSL))
        } ) }
      } ~
      path ("api" / "search" / "company" / Remaining) { token =>
        get { ctx => ( {
          log.debug("Performing company lookup")

          val companies = Company.queryByCompany(token)

          val companyList = companies match {
            case Nil => {
              //No matches in our database, use the markit api for lookup, filtering matches by company name
              val possibles = MarkitApi.lookup(token) filter ( _.companyName contains token)
              MarkitActor ! RetrieveByCompanyLookup(possibles)
              //use the Markit API for lookup
              val sortedUniqueCompanies = possibles groupBy(_.symbol) map(_._2.head) sort( (e1,e2) => (e1.symbol < e2.symbol))
              sortedUniqueCompanies map ( (company:Company) => (company.symbol -> company.companyName))
            }
            case _ => companies map ( (company:Company) => ( company.symbol -> company.companyName))
          }

          //create a dsl representing
          val resultDSL = (
            "resultCode" -> resultCodes.success ~
              "companies" -> companyList
            )
          ctx.complete(render(resultDSL))
        } ) }
      }
  }
  
}