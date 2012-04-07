package com.webwino.rest

import java.net.URLEncoder

import cc.spray._
import cc.spray.json._
import directives.Remaining
import cc.spray.utils.Logging

import com.webwino.models.User

trait Rest extends Directives with Logging {
  object resultCodes {
    val success = JsNumber(200)
    val userNotCreated = JsNumber(201)
    val failure = JsNumber(400)
    val idNotFound = JsNumber(401)
    val duplicateId = JsNumber(402)
  }

  private val fqClientId:String = "CW14C1TQVGHFR2CMXWWLJPFUSZNWE00P52BZAC2WPGFPGEK4"
  private val fqRedirectUri:String = URLEncoder.encode("http://localhost:8080/foursquare/callback", "UTF-8")
  
  val restService = {
    path("test") {
      get { _.complete("Say hello to Spray!") }
    } ~
    path ("foursquare" / "oauth") {
      get { ctx => ( {
        log.debug("returning foursquare params")
        ctx.complete(
          JsObject(Map("resultCode" -> resultCodes.success,
            "clientId" -> JsString(fqClientId),
            "callbackUri" -> JsString(fqRedirectUri)))
          .toString())
      } ) }
    } ~
    path ("foursquare" / "oauth" / Remaining) { token =>
      post { ctx => ( {
        User.fromAccessToken(token) match {
          case Some(user:User) => ( {
            JsObject(Map("resultCode" -> resultCodes.success) ++ user.toJson.fields)
          })
          case None => {
            JsObject("resultCode" -> resultCodes.userNotCreated)
          }
        }
      })
      }
    } ~
    path("api" / "users" / Remaining) { id =>
      get { ctx => ( {
        ctx.complete( (
          User.fromDb(id) match {
            case Some(user:User) => ( {
              val jsUser = user.toJson
              JsObject(Map("resultCode" -> resultCodes.success) ++ jsUser.fields)
            })
            case None => {
              JsObject("resultCode" -> resultCodes.idNotFound)
            }
          }
        ).toString() )
      } ) } ~
      put { ctx => ({
        User.fromDb(id) match {
          case Some(_) => ctx.complete(JsObject("resultCode" -> resultCodes.duplicateId).toString())
          case None => ( {
            val user = new User(id)
            User.toDb(user)
            ctx.complete(JsObject("resultCode" -> resultCodes.success).toString())
          })
        }
      })} ~
      post { ctx => ( {
        User.fromDb(id) match {
          case Some(_) => ctx.complete(JsObject("resultCode" -> resultCodes.success).toString())
          case None => ( {
            ctx.complete(JsObject("resultCode" -> resultCodes.idNotFound).toString())
          })
        }
      } ) } ~
        delete { ctx => ( {
          User.fromDb(id) match {
            case Some(user) => ( {
              User.delete(user)
              ctx.complete(JsObject("resultCode" -> resultCodes.success).toString())
            } )
            case None => ctx.complete(JsObject("resultCode" -> resultCodes.idNotFound).toString())
          }
        } ) }
    }
  }
  
}