package io.github.iamsurajgharat
package ruleevaluator
package models.web

import play.api.libs.json._

case class Rule(id:Option[String], expression:String, result:String)

object Rule {
    implicit val format = Json.format[Rule]
}