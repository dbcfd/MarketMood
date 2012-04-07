package com.webwino.actors

import akka.actor.Actor

import com.webwino.models.Company
import com.webwino.markit.{MarkitApi, LookupResult}

abstract class MarkitActorMessage
case class RetrieveBySymbolList(results:List[LookupResult]) extends MarkitActorMessage
case class RetrieveBySymbol(company:Company) extends MarkitActorMessage

class MarkitActor extends Actor {
  
  def receive = {
    case RetrieveBySymbolList(results) => {
      //group our results by symbol, so we can extract which exchanges we have data for
      val uniqueSymbols = results groupBy (_.symbol)
      uniqueSymbols foreach ( {
        //extract exchanges by mapping to list of strings
        val exchanges = _._2 map ( _.exchange )
        //create a company object
        val company = new Company(_._2.head.symbol, _._2.head.companyName, exchanges)
        //We need more queries and a database write, let some other thread handle it
        MarkitActor sendMessage company
      })
    }
    //handle our database write and queue up the data extraction for prices
    case RetrieveBySymbol(company) => {
      Company.toDb(company)
      //queue up our data extraction
      val company = new Company(result.symbol, result.)
    }
  }
}

object MarkitActor {
  def sendMessage(msg:MarkitActorMessage) {
    val system = ActorSystem("MySystem")
    val handler = system.actorOf(Props[MarkitActor])
    handler ! msg
  }  
}