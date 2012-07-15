package com.webwino.utilities

import cc.spray.typeconversion.{DefaultUnmarshallers, SimpleUnmarshaller}
import cc.spray.http.{HttpContent, ContentTypeRange}
import net.liftweb.json._
import cc.spray.http.MediaTypes.CustomMediaType

trait LiftJsonUnmarshaller {
  implicit val formats = DefaultFormats

  implicit def liftJsonUnmarshaller = new SimpleUnmarshaller[JValue] {
    val `application/json` = CustomMediaType("application/json")
    val canUnmarshalFrom = ContentTypeRange(`application/json`) :: Nil
    def unmarshal(content: HttpContent) = protect {
      val jsonSource = DefaultUnmarshallers.StringUnmarshaller(content).right.get
      parse(jsonSource)
    }
  }

}