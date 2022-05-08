package io.github.iamsurajgharat
package ruleevaluator
package models.web

import play.api.libs.json._
import io.github.iamsurajgharat.expressiontree.SExpType
import io.github.iamsurajgharat.expressiontree.expressiontree.DataType

case class Rule(id:String, expression:String, result:String)

object Rule {
    implicit val format = Json.format[Rule]
}

case class RuleMetadata(dataTypes:Map[String,DataType.DataType]) {
    def merge(that:RuleMetadata) = copy(dataTypes = dataTypes ++ that.dataTypes)
    def ++(that:RuleMetadata) = merge(that)
}

object RuleMetadata {
    val empty = new RuleMetadata(Map.empty)
    //implicit val format = Json.format[RuleMetadata]
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