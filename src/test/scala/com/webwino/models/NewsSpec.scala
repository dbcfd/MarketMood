package com.webwino.models

import org.specs2.mutable._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import net.liftweb.json.JsonAST.JInt

class NewsSpec extends Specification {
  "The News" should {
    "Create a news object" in {
      new News(new ObjectId(), "some url", new DateTime(), JInt(1)) must not beNull
    }

  }
}