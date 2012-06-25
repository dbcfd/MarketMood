package com.webwino
package mongo

import com.mongodb.casbah.Imports._

object Mongo {
   val mongoConn = MongoConnection()
   val companyCollection = mongoConn("marketMood")("companies")
   val newsCollection = mongoConn("marketMood")("news")
   val marketInformationCollection = mongoConn("marketMood")("marketInformation")
}
   