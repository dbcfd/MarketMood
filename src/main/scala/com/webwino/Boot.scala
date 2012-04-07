package com.webwino

import akka.config.Supervision._
import akka.actor.Supervisor
import akka.actor.Actor._
import cc.spray._

import com.webwino.rest.Rest
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

class Boot {
   RegisterJodaTimeConversionHelpers()
  
  val mainModule = new Rest {
    // bake your module cake here
  }

  val httpService = actorOf(new HttpService(mainModule.restService))
  val rootService = actorOf(new RootService(httpService))

  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(httpService, Permanent),
        Supervise(rootService, Permanent)
      )
    )
  )
}