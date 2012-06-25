package com.webwino.actors

import akka.actor._

import com.webwino.models.Company
import akka.routing.RoundRobinRouter
import cc.spray.can.client.HttpClient
import cc.spray.io.IoWorker
import cc.spray.client.HttpConduit
import cc.spray.http.{HttpResponse, HttpMethods, HttpRequest}

sealed trait CompanyLookupMessage
case class LookupCompany(symbol:String, name:String) extends CompanyLookupMessage

class CompanyLookup extends Actor {
  private val nbWorkers:Int = 10
  private val workerRouter = context.actorOf(Props[CompanyLookup].withRouter(RoundRobinRouter(nbWorkers)), name = "companyLookup")
  implicit val system:ActorSystem = CompanyLookup.system
  // every spray-can HttpClient (and HttpServer) needs an IoWorker for low-level network IO
  // (but several servers and/or clients can share one)
  val ioWorker = new IoWorker(system).start()

  // create and start a spray-can HttpClient
  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioWorker)),
    name = "http-client"
  )

  def receive = {
    case msg:LookupCompany => {
      //look up company on google finance
      // an HttpConduit gives us access to an HTTP server, it manages a pool of connections
      val conduit = new HttpConduit(httpClient, "github.com")

      // send a simple request
      val responseFuture = conduit.sendReceive(HttpRequest(method = HttpMethods.GET, uri = "/"))
      responseFuture.onComplete( (resp:Either[Throwable, HttpResponse]) => {
        conduit.close()
        resp
      })
      responseFuture.onSuccess( {
        case resp:HttpResponse => {
          //do something
        }
      } )
      /**
      log.info(
        """|Response for GET request to github.com:
          |status : {}
          |headers: {}
          |body   : {}""".stripMargin,
        response.status.value, response.headers.mkString("\n  ", "\n  ", ""), response.content
      )
       **/
    }
  }
}

object CompanyLookup {
  private val system = ActorSystem("LookupSystem")

  def sendMessage(msg:CompanyLookupMessage) {
    val handler = system.actorOf(Props[CompanyLookup], name = "lookup")
    handler ! msg
  }  
}