package com.webwino.markit

import org.specs2.mutable._
import cc.spray.test.SprayTest

class MarkitApiSpec extends Specification with SprayTest {
  "The MarkitAPI" should {
    "return a set of symbols for the query GM" in {
      MarkitApi.lookup("GM") must not be empty
    }
    "return no symbols for the query xqzrp" in {
      MarkitApi.lookup("xqzrp") must be empty
    }
    "return a quote for the query AAPL" in {
      MarkitApi.quote("AAPL") must beSome.which(_.Name == "Apple Inc")
    }
    "return nothing for the query xqzrp" in {
      MarkitApi.quote("xqzrp") must beNone
    }
    "return some for timeseries GOOG" in {
      MarkitApi.timeSeries("GOOG", 100) must beSome
    }
    "return some with duration matching for F" in {
      MarkitApi.timeSeries("F", 20) must beSome.which(_.SeriesDuration == 20) //anything between 11 and 19 seems to crash
    }
    "return some with open having values for BA" in {
      MarkitApi.timeSeries("BA", 100) must beSome.which(_.Series.open.values.length > 0 )
    }
  }

}