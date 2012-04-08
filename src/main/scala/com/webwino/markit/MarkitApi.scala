package com.webwino.markit

import org.apache.commons.httpclient._
import org.apache.commons.httpclient.methods._
import net.liftweb.json._
import net.liftweb.json.JsonParser.ParseException
import java.io._
import scala.io.Source

case class MarkitError(Message:String)
case class QuoteResult(Name:String, Symbol:String, LastPrice:Double, Timestamp:String, High:Double, Low:Double, Open:Double)
case class LookupResult(Symbol:String, Name:String, Exchange:String)
case class PriceData(min:Double,max:Double, values:List[Double])
case class SeriesData(open:PriceData,high:PriceData,low:PriceData,close:PriceData)
case class TimeSeriesResult(Name:String, Symbol:String, Series:SeriesData, SeriesDuration:Int, SeriesDates:List[String])

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
    try {
      val rspJson:JValue = parse(method.getResponseBodyAsString())
      for(rspObj <- rspJson.children) yield rspObj.extract[LookupResult]
    }
    catch {
      case ex: MappingException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        //JsonParser.parse(response).extract[MarkitError]
        Nil
      })
      case ex: ParseException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        //JsonParser.parse(response).extract[MarkitError]
        Nil
      })
      //let other errors propagate back
    }
  }

  /**
   * Perform Quote API function
   * @param symbol Symbol to receive quote for
   * @return List of QuoteResults. Timestamps are js date object (essentially a string) which should be converted
   * to a usable JodaDate
   */
  def quote(symbol:String):Option[QuoteResult] = {
    val apiUrl = "http://dev.markitondemand.com/Api/Quote/json?symbol=" + symbol
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    try {
      val rspJson:JValue = parse(method.getResponseBodyAsString())
      Some( (rspJson \ "Data").extract[QuoteResult] )
    }
    catch {
      case ex: MappingException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        //JsonParser.parse(response).extract[MarkitError]
        val what = ex.getMessage()
        None
      } )
      case ex: ParseException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        //JsonParser.parse(response).extract[MarkitError]
        None
      })
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
    val apiUrl = "http://dev.markitondemand.com/Api/Timeseries/json?symbol=" + symbol + "&duration=" + duration
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    try {
      val rspJson:JValue = parse(method.getResponseBodyAsString())
      Some( (rspJson \ "Data").extract[TimeSeriesResult])
    }
    catch {
      case ex: MappingException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        //JsonParser.parse(response).extract[MarkitError]
        None
      } )
      case ex: ParseException => ( {
        //rather than returning an error code, markit returns an error response
        //grab the error so we can log it
        //JsonParser.parse(response).extract[MarkitError]
        None
      })
      //let other errors propagate back
    }
  }
}