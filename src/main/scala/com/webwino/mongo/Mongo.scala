package com.webwino
package mongo

import com.mongodb.casbah.Imports._

object Mongo {
   val mongoConn = MongoConnection()
   val userCollection = mongoConn("locaquest")("users")
   val mobiCollection = mongoConn("locaquest")("monsterInPlay")
   val mobdCollection = mongoConn("locaquest")("monsterDescription")
}
   