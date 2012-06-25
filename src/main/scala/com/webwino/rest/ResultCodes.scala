package com.webwino.rest

import net.liftweb.json.JsonAST.JInt

object ResultCodes {
  val success = JInt(0)
  val failure = JInt(-1)
}