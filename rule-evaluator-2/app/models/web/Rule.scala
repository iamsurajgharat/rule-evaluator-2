package io.github.iamsurajgharat
package ruleevaluator
package models.web

import play.api.libs.json._

case class Rule(id:String, expression:String, result:String)

object Rule {
    implicit val format = Json.format[Rule]
}

sealed trait BaseDTO{
    val num2:Int = 10
}

case class SaveRulesResponseDTO(successIds:List[String], errors:Map[String,String]) extends BaseDTO

object SaveRulesResponseDTO {
    implicit val format = Json.format[SaveRulesResponseDTO]
}

case class GetRulesResponseDTO(data:List[Rule]) extends BaseDTO

object GetRulesResponseDTO {
    implicit val format = Json.format[GetRulesResponseDTO]
}