package com.webwino.actors

import akka.actor._

import com.webwino.models.Company
import akka.routing.RoundRobinRouter
import org.joda.time.DateTime

sealed trait NewsLookupMessage
case class LookupNews(company:Company, date:DateTime) extends NewsLookupMessage

class NewsLookup extends Actor {
  def receive = {
    //find news, write to database
    case LookupNews(company, date) => {

    }
  }
}

object NewsLookup {
  private val system = ActorSystem("LookupSystem")

  def sendMessage(msg:LookupMarketInformation) {
    val handler = system.actorOf(Props[NewsLookup], name = "lookup")
    handler ! msg
  }
}