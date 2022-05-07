package io.github.iamsurajgharat
package ruleevaluator
package models.web

import play.api.libs.json._

case class Rule(id:String, expression:String, result:String)

object Rule {
    implicit val format = Json.format[Rule]
}

case class SaveRulesResponseDTO(successIds:List[String], errors:Map[String,String])

object SaveRulesResponseDTO {
    implicit val format = Json.format[SaveRulesResponseDTO]
}