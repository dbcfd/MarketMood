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
class News(val dbObject: MongoDBObject) {
  object Mood
  {
    val Negative = JInt(-1)
    val Neutral = JInt(0)
    val Positive = JInt(1)
  }

  def this(companyId:ObjectId, newsUrl:String, newsDate:DateTime, newsMood:JInt) = this(MongoDBObject(
    "companyId" -> companyId,
    "url" -> newsUrl,
    "date" -> newsDate,
    "mood" -> newsMood
  ))

  def companyId: ObjectId = dbObject.as[ObjectId]("companyId")

  def url: String = dbObject.as[String]("url")

  def date: DateTime = dbObject.as[DateTime]("date")

  def mood: JInt = dbObject.as[JInt]("mood")

  def id: ObjectId = dbObject.as[ObjectId]("_id")
}

/**
 * Companion class used to perform database access
 */
object News {
  implicit val formats = DefaultFormats

  val collection = Mongo.newsCollection

  def fromDb(companyId: ObjectId): List[News] = {
    val newsDBObject: MongoDBObject = MongoDBObject("companyId" -> companyId)
    collection.find(newsDBObject)
    for (x <- collection toList) yield (new News(x))
  }

  def fromDb(companyId: ObjectId, newsDate:DateTime): List[News] = {
    val newsDBObject: MongoDBObject = MongoDBObject("companyId" -> companyId, "date" -> newsDate)
    collection.find(newsDBObject)
    for (x <- collection toList) yield (new News(x))
  }

  def toDb(news: MongoDBObject) = {
    collection.save(news)
  }

  def delete(news: MongoDBObject) = {
    collection.remove(news)
  }

  /**
   * Implicits to handle conversion between wrappers and database objects
   */
  implicit def dbToNews(dbObj: MongoDBObject): News = new News(dbObj)

  implicit def newsToDb(obj: News): MongoDBObject = obj.dbObject
}