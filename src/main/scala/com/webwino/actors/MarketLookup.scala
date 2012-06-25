package com.webwino.actors

import akka.actor._

import com.webwino.models.Company
import akka.routing.RoundRobinRouter

sealed trait MarketLookupMessage
case class LookupMarketInformation(company:Company) extends MarketLookupMessage

class MarketLookup extends Actor {
  private val nbWorkers:Int = 10
  private val workerRouter = context.actorOf(Props[MarketLookup].withRouter(RoundRobinRouter(nbWorkers)), name = "marketLookup")

  def receive = {
    case LookupMarketInformation(company) => {

    }
  }
}

object MarketLookup {
  private val system = ActorSystem("LookupSystem")

  def sendMessage(msg:LookupMarketInformation) {
    val handler = system.actorOf(Props[MarketLookup], name = "lookup")
    handler ! msg
  }
}