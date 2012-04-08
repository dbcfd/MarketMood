package com.webwino
package mongo

import com.mongodb.casbah.Imports._

object Mongo {
   val mongoConn = MongoConnection()
   val companyCollection = mongoConn("markit")("stockData")
}
   