package com.webwino.models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

import com.webwino.mongo.Mongo
import cc.spray.json._

class User(val dbObject:MongoDBObject) {
  def this(fqId:String) = this(MongoDBObject("foursquareId" -> fqId))
  def foursquareId:String = dbObject.as[String]("foursquareId")
  def accessToken:String = dbObject.as[String]("accessToken")
  def id:ObjectId = dbObject.as[ObjectId]("_id")

  def toJson = JsObject("_id" -> JsString(id.toString()), "foursquareId" -> JsString(foursquareId))
}

object User {
  val collection = Mongo.userCollection
  
  def fromDb(foursquareId:String):Option[User] = {
    val dbUser = MongoDBObject("foursquareId" -> foursquareId)
    val found = collection.findOne(dbUser)
    found match {
      case Some(obj:DBObject) => ({
        Some(new User(obj))
      } )
      case None => None
    }
  }

  def fromAccessToken(accessToken:String):Option[User] = {
    val dbUser = MongoDBObject("accessToken" -> accessToken)
    val found = collection.findOne(dbUser)
    found match {
      case Some(obj:DBObject) => ({
        Some(new User(obj))
      } )
      case None => None
    }
  }

  def fromJson(v:JsValue) = v.asJsObject.getFields("foursquareId") match {
    case Seq(JsString(foursquareId)) => ( {
      val dbObj = MongoDBObject("foursquareId" -> foursquareId)
      new User(dbObj)
    } )
    case _ => deserializationError("Incorrect format:" + v.toString())
  }

  def toDb(user:MongoDBObject) = {
    collection.save(user)
  }
  
  def delete(user:MongoDBObject) = {
    collection.remove(user)
  }
  
  implicit def dbToUser(dbObj:MongoDBObject):User = new User(dbObj)
  implicit def userToDb(user:User):MongoDBObject = user.dbObject
}