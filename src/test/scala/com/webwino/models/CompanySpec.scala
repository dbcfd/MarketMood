package com.webwino.models

import org.specs2.mutable._

class CompanySpec extends Specification {
  "The Company" should {
    "create a company" in {
      new Company(Nil, Nil) must not beNull
    }
    "create a company when creating with symbols and names that has one of the symbols" in {
      (new Company(List[String]("GM", "NYSE:GM"),
        List[String]("General Motors", "GM", "General Motors, Inc.") )) toString() must /("symbols") */ ("NYSE:GM")
    }
    "create a company when creating with symbols and names that has one of the names" in {
      (new Company(List[String]("GM", "NYSE:GM"),
        List[String]("General Motors", "GM", "General Motors, Inc.") )) toString() must /("names") */ ("General Motors, Inc.")
    }
  }
}