package com.webwino.markit

import org.apache.commons.httpclient._
import org.apache.commons.httpclient.methods._
import net.liftweb.json._
import java.io._

case class MarkitError(msg:String)
case class QuoteResult(status:String, name:String, symbol:String, price:Double, timestamp:String, high:Double, low:Double, open:Double)
case class QuoteResults(results:List[QuoteResult])
case class LookupResult(symbol:String, name:String, exchange:String)
case class LookupResults(results:List[LookupResult])
case class OHLC(open:Double, high:Double, low:Double: close:Double)
case class TimeSeriesResult(name:String, symbol:String, ohlcs:List[OHLC}, dates:List[String])

/**
 * Encapsulate the Markit REST API (http://dev.markitondemand.com)
 */
object MarkitApi {
  implicit val formats = DefaultFormats //json formatting
  val client = new HttpClient()

  /**
   * Perform Lookup API function
   * @param symbolOrCompanyPartName MultiSearch object (indicated by OR) matching either symbol or a portion of the
   * company name
   * @return List of possible matches. Includes multiple indexes as well, so if GM is on NYSE and BATS, two results are
   * returned
   */
  def lookup(symbolOrCompanyPartName:String):List[LookupResult] = {
    val apiUrl = "http://dev.markitondemand.com/Api/Lookup/json?input=" + symbolOrCompanyPartName
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    val response:Reader = new InputStreamReader(method.getResponseBodyAsStream())
    try {
      JsonParser.parse(response).extract[LookupResults].results
    }
    catch {
      case ex: MappingException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        JsonParser.parse(response).extract[MarkitError]
        Nil //return empty list
      } )
      //let other errors propagate back
    }
  }

  /**
   * Perform Quote API function
   * @param symbol Symbol to receive quote for
   * @return List of QuoteResults. Timestamps are js date object (essentially a string) which should be converted
   * to a usable JodaDate
   */
  def quote(symbol:String):List[QuoteResult] = {
    val apiUrl = "http://dev.markitondemand.com/Api/Quote/json?symbol=" + symbol
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    val response:Reader = new InputStreamReader(method.getResponseBodyAsStream())
    try {
      JsonParser.parse(response).extract[QuoteResults].results
    }
    catch {
      case ex: MappingException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        JsonParser.parse(response).extract[MarkitError]
        Nil //return empty list
      } )
      //let other errors propagate back
    }
  }

  /**
   * Perform Timeseries API functionality
   * @param symbol Symbol to receive time series for
   * @param duration Duration in integer (min:10, max:??? Guessing start of data) for days
   * @return Option indicating time series was found matching options or error
   */
  def timeSeries(symbol:String, duration:Int):Option[TimeSeriesResult] = {
    val apiUrl = "http://dev.markitondemand.com/Api/TimeSeries/json?symbol=" + symbol + "+duration=" + duration
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    val response:Reader = new InputStreamReader(method.getResponseBodyAsStream())
    try {
      Some(JsonParser.parse(response).extract[TimeSeriesResult])
    }
    catch {
      case ex: MappingException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        JsonParser.parse(response).extract[MarkitError]
        None //return empty list
      } )
      //let other errors propagate back
    }
  }
}