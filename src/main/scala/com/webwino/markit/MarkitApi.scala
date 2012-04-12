package com.webwino.markit

import akka.actor.{Props, ActorSystem}
import cc.spray.can.model.{HttpRequest, HttpMethods, HttpResponse}
import net.liftweb.json._
import net.liftweb.json.JsonParser.ParseException
import java.io._
import cc.spray.io.IoWorker
import akka.dispatch.{Await, Future}
import akka.util.duration._
import cc.spray.can.client.{HttpDialog, HttpClient}
import cc.spray.RequestContext
import com.webwino.models.Company

case class MarkitError(Message: String)
case class QuoteResult(Name: String, Symbol: String, LastPrice: Double, Timestamp: String, High: Double, Low: Double, Open: Double)
case class LookupResult(Symbol: String, Name: String, Exchange: String)
case class PriceData(min: Double, max: Double, values: List[Double])
case class SeriesData(open: PriceData, high: PriceData, low: PriceData, close: PriceData)
case class TimeSeriesResult(Name: String, Symbol: String, Series: SeriesData, SeriesDuration: Int, SeriesDates: List[String])

/**
 * Encapsulate the Markit REST API (http://dev.markitondemand.com)
 */
object MarkitApi {
  implicit val formats = DefaultFormats //json formatting
  implicit val system = ActorSystem()

  def log = system.log

  val ioWorker = new IoWorker(system).start()
  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioWorker)),
    name = "http-client"
  )

  /**
   * Perform Lookup API function
   * @param symbolOrCompanyPartName MultiSearch object (indicated by OR) matching either symbol or a portion of the
   * company name
   * @return List of possible matches. Includes multiple indexes as well, so if GM is on NYSE and BATS, two results are
   * returned
   */
  def lookup(symbolOrCompanyPartName: String) : List[LookupResult] = {
    val responseFuture = lookupFuture(symbolOrCompanyPartName)
    val done = Await.ready(responseFuture, 10 seconds)
    done.value match {
      case Some(eitherResponse) => eitherResponse match {
        case Right(response) => {
          val responseString = new String(response.body)
          log.debug(responseString)
          try {
            val rspJson: JValue = parse (responseString)
            for (rspObj <- rspJson.children) yield rspObj.extract[LookupResult]
          }
          catch {
            case ex: MappingException => Nil //TODO log
            case ex: ParseException => Nil //TODO log
          }
        }
        case Left(error) => {
          //TODO log
          Nil
        }
      }
      case None => Nil
    }
  }
  def lookup(symbolOrCompanyPartName: String, handle: (List[LookupResult]) => Unit) {
    val responseFuture = lookupFuture(symbolOrCompanyPartName)
    responseFuture.onComplete { result =>
      result match {
        case Right(response) => {
          try {
            val rspJson: JValue = parse (response.asInstanceOf[String] )
            val results = for (rspObj <- rspJson.children) yield rspObj.extract[LookupResult]
            handle(results)
          }
          catch {
            case ex: MappingException => Nil //TODO log
            case ex: ParseException => Nil //TODO log
          }
        }
        case Left(fError) => {
          log.error("Could not get response due to {}", fError)
          Nil
        }
      }
    }
  }
  def lookupFuture(symbolOrCompanyPartName: String):Future[HttpResponse] = {
    HttpDialog(httpClient, "dev.markitondemand.com")
      .send(HttpRequest(method = HttpMethods.GET, uri = "/Api//Lookup/json?input=" + symbolOrCompanyPartName))
      .end
  }

  /**
   * Perform Quote API function
   * @param symbol Symbol to receive quote for
   * @return List of QuoteResults. Timestamps are js date object (essentially a string) which should be converted
   * to a usable JodaDate
   */
  def quote(symbol: String): Option[QuoteResult] = {
    /**
    val apiUrl = "http://dev.markitondemand.com/Api/Quote/json?symbol=" + symbol
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    val responseAsByteArray = readWholeStream(method.getResponseBodyAsStream())
    method.releaseConnection()
    val responseAsString = new String(responseAsByteArray)
    try {
      val rspJson:JValue = parse(responseAsString)
      Some( (rspJson \ "Data").extract[QuoteResult] )
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
     **/
    None
  }

  /**
   * Perform Timeseries API functionality
   * @param symbol Symbol to receive time series for
   * @param duration Duration in integer (min:10, max:??? Guessing start of data) for days
   * @return Option indicating time series was found matching options or error
   */
  def timeSeries(symbol: String, duration: Int): Option[TimeSeriesResult] = {
    /**
    val apiUrl = "http://dev.markitondemand.com/Api/Timeseries/json?symbol=" + symbol + "&duration=" + duration
    val method = new GetMethod(apiUrl)
    client.executeMethod(method)
    val responseAsByteArray = readWholeStream(method.getResponseBodyAsStream())
    method.releaseConnection()
    val responseAsString = new String(responseAsByteArray)
    try {
      val rspJson:JValue = parse(responseAsString)
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
     **/
    None
  }
}