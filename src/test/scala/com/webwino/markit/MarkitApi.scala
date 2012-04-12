package com.webwino.markit

import org.specs2.mutable._
import cc.spray.test.SprayTest

class MarkitApiSpec extends Specification with SprayTest {
  "The MarkitAPI" should {
    "return a set of symbols for the query GM" in {
      MarkitApi.lookupWithWait("GM") must not be empty
    }
    "return no symbols for the query xqzrp" in {
      MarkitApi.lookupWithWait("xqzrp") must be empty
    }
    "return a list of quotes with one having Symbol AAPL for the query AAPL" in {
      MarkitApi.quoteWithWait("AAPL") must have(_.Symbol == "AAPL")
    }
    "return nothing for the query xqzrp" in {
      MarkitApi.quoteWithWait("xqzrp") must be empty
    }
    "return a list of quotes with one having Symbol GOOG for timeseries GOOG" in {
      MarkitApi.timeSeriesWithWait("GOOG", 100) must have(_.Symbol == "GOOG")
    }
    "return a list of quotes with one having SeriesDuration 20 for timeseries F" in {
      MarkitApi.timeSeriesWithWait("F", 20) must have(_.SeriesDuration == 20) //anything between 11 and 19 seems to crash
    }
    "return a list of quotes with one having a Series open head value greater than 0 for timeseries BA" in {
      MarkitApi.timeSeriesWithWait("BA", 100) must have(_.Series.open.values.head > 0)
    }
  }

}