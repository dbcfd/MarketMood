package com.webwino.models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import net.liftweb.json._

import com.webwino.mongo.Mongo
import org.joda.time.DateTime

/**
 * Wrapper around the database object for each company
 * @param dbObject Mongo object to create wrapper from
 */
class MarketInformation(val dbObject: MongoDBObject) {
  def this(companyId:ObjectId, date:DateTime, open:JDouble, high:JDouble, low:JDouble, close:JDouble, volume:JDouble) = this(MongoDBObject(
    "companyId" -> companyId,
    "date" -> date,
    "open" -> open,
    "high" -> high,
    "low" -> low,
    "close" -> close
  ))

  def companyId: ObjectId = dbObject.as[ObjectId]("companyId")

  def date:DateTime = dbObject.as[DateTime]("date")

  def open:JDouble = dbObject.as[JDouble]("open")
  def high:JDouble = dbObject.as[JDouble]("high")
  def low:JDouble = dbObject.as[JDouble]("low")
  def close:JDouble = dbObject.as[JDouble]("close")

  def id: ObjectId = dbObject.as[ObjectId]("_id")
}

/**
 * Companion class used to perform database access
 */
object MarketInformation {
  implicit val formats = DefaultFormats

  val collection = Mongo.marketInformationCollection

  def fromDb(companyId: ObjectId): List[MarketInformation] = {
    val miDBObject: MongoDBObject = MongoDBObject("companyId" -> companyId)
    collection.find(miDBObject)
    for (x <- collection toList) yield (new MarketInformation(x))
  }

  def fromDb(companyId: ObjectId, marketDate:DateTime): List[MarketInformation] = {
    val miDBObject: MongoDBObject = MongoDBObject("companyId" -> companyId, "date" -> marketDate)
    collection.find(miDBObject)
    for (x <- collection toList) yield (new MarketInformation(x))
  }

  def toDb(mi: MongoDBObject) = {
    collection.save(mi)
  }

  def delete(mi: MongoDBObject) = {
    collection.remove(mi)
  }

  /**
   * Implicits to handle conversion between wrappers and database objects
   */
  implicit def dbToMarketInformation(dbObj: MongoDBObject): MarketInformation = new MarketInformation(dbObj)

  implicit def marketInformationToDb(obj: MarketInformation): MongoDBObject = obj.dbObject
}