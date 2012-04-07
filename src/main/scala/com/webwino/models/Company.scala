package com.webwino.models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

import com.webwino.mongo.Mongo
import net.liftweb.json._

/**
 * Wrapper around the database object for each company
 * @param dbObject Mongo object to create wrapper from
 */
class Company(val dbObject:MongoDBObject) {
  def this(sym:String, name:String, exchanges:List[String}) = this(MongoDBObject(
    "symbol" -> sym,
    "companyName" -> name,
    "exchanges" -> exchanges
  ))
  def symbol:String = dbObject.as[String]("symbol")
  def companyName:String = dbObject.as[String]("companyName")
  def exchanges:List[String] = dbObject.as[List[String]]("exchanges")
  def id:ObjectId = dbObject.as[ObjectId]("_id")
}

/**
 * Companion class used to perform database access
 */
object Company {
  val collection = Mongo.companyCollection

  /**
   * Get all possible companies in the database
   * @return
   */
  def getAllCompanies():List[(String,String)] = {
    val findAll = MongoDBObject("_id" -> 1)
    val companies = collection.find(findAll)
    companies map ( (obj:DBObject) => new Company(obj))
  }
  
  def queryBySymbol(symbol:String):List[(String,String)] = {
    val q = "symbol" $like symbol
    val companies = collection.find(q))
    companies map ( (obj:DBObject) => new Company(obj))
  }

  def queryByCompanyName(name:String):List[(String,String)] = {
    val q = "name" $like symbol
    val companies = collection.find(q))
    companies map ( (obj:DBObject) => new Company(obj))
  }

  /**
   * Retrieve a specific company based on its symbol
   * @param symbol
   * @return
   */
  def fromDb(symbol:String):Option[Company] = {
    val dbUser:MongoDBObject = MongoDBObject("symbol" -> symbol)
    val found = collection.findOne(dbUser)
    found match {
      case Some(obj:DBObject) => ({
        Some(new Company(obj))
      } )
      case None => None
    }
  }

  def toDb(user:MongoDBObject) = {
    collection.save(user)
  }
  
  def delete(user:MongoDBObject) = {
    collection.remove(user)
  }

  /**
   * Implicits to handle conversion between wrappers and database objects
   */
  implicit def dbToCompany(dbObj:MongoDBObject):Company = new Company(dbObj)
  implicit def companyToDb(obj:Company):MongoDBObject = obj.dbObject
}