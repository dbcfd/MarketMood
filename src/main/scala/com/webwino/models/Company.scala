package com.webwino.models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

import com.webwino.mongo.Mongo
import org.joda.time.DateTime
import net.liftweb.json._

/**
 * Wrapper around the database object for each company
 * @param dbObject Mongo object to create wrapper from
 */
class Company(val dbObject: MongoDBObject) {
  def this(symbols:List[String], names:List[String]) = this(MongoDBObject(
    "symbols" -> symbols,
    "names" -> names
  ))

  def names: List[String] = dbObject.as[List[String]]("names")
  def symbols: List[String] = dbObject.as[List[String]]("symbols")
  def id: ObjectId = dbObject.as[ObjectId]("_id")
}

/**
 * Companion class used to perform database access
 */
object Company {
  implicit val formats = DefaultFormats

  val collection = Mongo.companyCollection

  /**
   * Get all possible companies in the database
   * @return
   */
  def getAllCompanies(): List[Company] = {
    val findAll = MongoDBObject("_id" -> 1)
    collection.find(findAll)
    for (x <- collection toList) yield (new Company(x))
  }

  def queryBySymbol(symbol: String): List[Company] = {
    collection.find(MongoDBObject("symbols" -> (".*" + symbol + ".*").r))
    for (x <- collection toList) yield (new Company(x))
  }

  /**
   * Retrieve a specific company based on its symbol
   * @param symbol
   * @return
   */
  def fromDb(symbol: String): Option[Company] = {
    val dbUser: MongoDBObject = MongoDBObject("symbols" -> symbol)
    val found = collection.findOne(dbUser)
    found match {
      case Some(obj: DBObject) => ({
        Some(new Company(obj))
      })
      case None => None
    }
  }

  def toDb(company: MongoDBObject) = {
    collection.save(company)
  }

  def delete(company: MongoDBObject) = {
    collection.remove(company)
  }

  /**
   * Implicits to handle conversion between wrappers and database objects
   */
  implicit def dbToCompany(dbObj: MongoDBObject): Company = new Company(dbObj)

  implicit def companyToDb(obj: Company): MongoDBObject = obj.dbObject

  implicit def jsonStringToCompany(jsonString:String): Company = {
    val jsonObj = JsonParser.parse(jsonString)
    jsonToCompany(jsonObj)
  }

  implicit def jsonToCompany(json:JValue): Company = {
    new Company(
      (json \ "symbols").extract[List[String]],
      (json \ "names").extract[List[String]]
        )
  }

  implicit def companyToJson(obj: Company): JValue = {
    val json = JObject(List(
      JField("symbols", JArray(obj.symbols map (JString(_)))),
      JField("names", JArray(obj.symbols map (JString(_)))),
      JField("id", JString(obj.id.toString))
    ))
    json
  }

  implicit def companyToJsonString(obj: Company): String = {
    compact(render(companyToJson(obj)))
  }
}