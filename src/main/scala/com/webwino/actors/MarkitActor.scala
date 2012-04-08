package com.webwino.actors

import akka.actor._

import com.webwino.models.Company
import com.webwino.markit.{MarkitApi, LookupResult}
import akka.routing.RoundRobinRouter

sealed trait MarkitActorMessage
case class RetrieveBySymbolList(results:List[LookupResult]) extends MarkitActorMessage
case class RetrieveBySymbol(company:Company) extends MarkitActorMessage

class MarkitActor() extends Actor {
  private val nbWorkers:Int = 10
  private val workerRouter = context.actorOf(Props[MarkitWorker].withRouter(RoundRobinRouter(nbWorkers)), name = "workerRouter")

  def receive = {
    case RetrieveBySymbolList(results) => {
      //group our results by symbol, so we can extract which exchanges we have data for
      val uniqueSymbols = results groupBy (_.Symbol)
      uniqueSymbols foreach ( mapObj => {
        //extract exchanges by mapping to list of strings
        val exchanges = mapObj._2 map ( (res:LookupResult) => res.Exchange )
        //create a company object
        val company = new Company(mapObj._2.head.Symbol, mapObj._2.head.Name, exchanges)
        //We need more queries and a database write, let some other thread handle it
        workerRouter ! RetrieveBySymbol(company)
      })
    }

  }

  class MarkitWorker extends Actor {
    def receive = {
      //handle our database write and queue up the data extraction for prices
      case RetrieveBySymbol(company) => {
        Company.toDb(company)
        //queue up our data extraction
      }
    }
  }
}

object MarkitActor {
  private val system = ActorSystem("MarkitSystem")

  def sendMessage(msg:MarkitActorMessage) {
    val handler = system.actorOf(Props[MarkitActor], name = "markit")
    handler ! msg
  }  
}