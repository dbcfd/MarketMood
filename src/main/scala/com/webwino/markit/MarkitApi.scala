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
abstract class MarkitResult
case class QuoteResult(Name: String, Symbol: String, LastPrice: Double, Timestamp: String, High: Double, Low: Double, Open: Double) extends MarkitResult
case class LookupResult(Symbol: String, Name: String, Exchange: String) extends MarkitResult
case class PriceData(min: Double, max: Double, values: List[Double])
case class SeriesData(open: PriceData, high: PriceData, low: PriceData, close: PriceData)
case class TimeSeriesResult(Name: String, Symbol: String, Series: SeriesData, SeriesDuration: Int, SeriesDates: List[String]) extends MarkitResult

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
   * Request a future from an HttpClient connection to the markit api, based on some uri. This
   * function will then wait until the request is complete, or 10 seconds have passed. It will then
   * see if a value is available, using a function to convert the parsed json into a list
   * @param uri Markit API uri to query, e.g. /Api/Lookup/json?input=Google
   * @param handle Function to invoke after wait is complete, converting JValue to a list of objects
   * @tparam T List type, derived from MarkitResult
   * @return List of T objects that result from invoking handle
   */
  def handleRequestOnWait[T <: MarkitResult](uri:String, handle:(JValue)=>List[T]):List[T] = {
    val responseFuture = requestFuture(uri)
    val done = Await.ready(responseFuture, 10 seconds)
    done.value match {
      case Some(eitherResponse) => eitherResponse match {
        case Right(response) => {
          val responseString = new String(response.body)
          log.debug(responseString)
          try {
            val rspJson: JValue = parse (responseString)
            handle(rspJson)
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

  /**
   * Request a future from an HttpClient connection to the markit api, based on some uri. This
   * function will utilize the futures asynchronous onComplete method to invoke a function to handle a json value.
   * This functionality is useful with sprays completeWith functionality, to only return a result once the markit
   * query has completed
   * @param uri Markit API uri to query, e.g. /Api/Lookup/json?input=Google
   * @param handle Function to handle the json value returned from the HttpClient request
   */
  def handleRequestOnComplete(uri:String, handle:(JValue)=>Unit) {
    val responseFuture = requestFuture(uri)
    responseFuture.onComplete { result =>
      result match {
        case Right(response) => {
          try {
            val rspJson: JValue = parse (response.asInstanceOf[String] )
            handle(rspJson)
          }
          catch {
            case ex: MappingException => () //TODO log
            case ex: ParseException => () //TODO log
          }
        }
        case Left(fError) => {
          log.error("Could not get response due to {}", fError)
          ()
        }
      }
    }
  }

  /**
   * Create a connection with the markit api, performing a GET to a uri, and obtaining a future that is complete
   * when the request completes
   * @param queryUri Uri of the query, such as Api/Quote/json?input=F
   * @return Future containing the HttpResponse from the HttpRequest
   */
  def requestFuture(queryUri:String):Future[HttpResponse] = {
    HttpDialog(httpClient, "dev.markitondemand.com")
      .send(HttpRequest(method = HttpMethods.GET, uri = queryUri))
      .end
  }

  /**
   * Perform Lookup API function
   * @param symbolOrCompanyPartName MultiSearch object (indicated by OR) matching either symbol or a portion of the
   * company name
   * @return List of possible matches. Includes multiple indexes as well, so if GM is on NYSE and BATS, two results are
   * returned
   */
  def lookupWithWait(symbolOrCompanyPartName: String) : List[LookupResult] = {
    handleRequestOnWait[LookupResult]("/Api/Lookup/json?input=" + symbolOrCompanyPartName, (rspJson:JValue) => {
      for (rspObj <- rspJson.children) yield rspObj.extract[LookupResult]
    } )
  }
  def lookup(symbolOrCompanyPartName: String, handle: (List[LookupResult]) => Unit) {
    handleRequestOnComplete("/Api/Lookup/json?input=" + symbolOrCompanyPartName, (rspJson:JValue) => {
      val results = for (rspObj <- rspJson.children) yield rspObj.extract[LookupResult]
      handle(results)
    } )
  }

  /**
   * Perform Quote API function
   * @param symbol Symbol to receive quote for
   * @return List of QuoteResults. Timestamps are js date object (essentially a string) which should be converted
   * to a usable JodaDate
   */
  def quoteWithWait(symbol: String) : List[QuoteResult] = {
    handleRequestOnWait[QuoteResult]("/Api/Quote/json?symbol=" + symbol, (rspJson:JValue) => {
      List((rspJson \ "Data").extract[QuoteResult])
    } )
  }
  def quote(symbol: String, handle: (List[QuoteResult]) => Unit) {
    handleRequestOnComplete("/Api/Lookup/json?symbol=" + symbol, (rspJson:JValue) => {
      handle(List((rspJson \ "Data").extract[QuoteResult]))
    } )
  }

  /**
   * Perform Timeseries API functionality
   * @param symbol Symbol to receive time series for
   * @param duration Duration in integer (min:10, max:??? Guessing start of data) for days
   * @return Option indicating time series was found matching options or error
   */
  def timeSeriesWithWait(symbol: String, duration:Int) : List[TimeSeriesResult] = {
    handleRequestOnWait[TimeSeriesResult]("/Api/Timeseries/json?symbol=" + symbol + "&duration=" + duration, (rspJson:JValue) => {
      List((rspJson \ "Data").extract[TimeSeriesResult])
    } )
  }
  def timeSeries(symbol: String, duration:Int, handle: (List[TimeSeriesResult]) => Unit) {
    handleRequestOnComplete("/Api/Timeseries/json?symbol=" + symbol + "&duration=" + duration, (rspJson:JValue) => {
      handle(List((rspJson \ "Data").extract[TimeSeriesResult]))
    } )
  }
}