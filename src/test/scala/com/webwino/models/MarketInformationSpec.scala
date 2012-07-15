package com.webwino.models

import org.specs2.mutable._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import net.liftweb.json.JsonAST.JDouble

class MarketInformationSpec extends Specification {
  "The MarketInformation" should {
    "Create a MarketInformation object" in {
      new MarketInformation(new ObjectId(), new DateTime(),
        JDouble(0), JDouble(0), JDouble(0), JDouble(0), JDouble(0)) must not beNull
    }

  }
}