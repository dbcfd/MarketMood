package com.webwino

import cc.spray._

import com.webwino.rest.Rest
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import akka.actor.{Props, ActorSystem}

class Boot(system:ActorSystem) {
   RegisterJodaTimeConversionHelpers()
  
  val mainModule = new Rest {
    implicit def actorSystem = system
    // bake your module cake here
  }

  val httpService = system.actorOf(
    props = Props(new HttpService(mainModule.restService)),
    name = "http-server"
  )
  val rootService = system.actorOf(
    props = Props(new RootService(httpService)),
    name="spray-root-service"
  )

  system.registerOnTermination {
    //put cleanup code here
    system.log.info("Application shut down")
  }
}