package com.webwino.rest

import org.specs2.mutable._
import com.webwino.models.Company

class CompanySpec extends Specification {
  "The Company" should {
    "create a company when creating with symbols and names that has one of the symbols" in {
      new Company(List[String]("GM", "NYSE:GM"), List[String]("General Motors", "GM", "General Motors, Inc.") )
      must /("symbols") */ ("NYSE:GM")
    }
    "create a company when creating with symbols and names that has one of the names" in {
      new Company(List[String]("GM", "NYSE:GM"), List[String]("General Motors", "GM", "General Motors, Inc.") )
      must /("names") */ ("General Motors, Inc.")
    }
  }
}