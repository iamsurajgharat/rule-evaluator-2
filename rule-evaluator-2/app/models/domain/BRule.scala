package io.github.iamsurajgharat
package ruleevaluator
package models.domain

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.expressiontree.expressiontree.CExpressionImpl
import play.api.libs.json.Json

class BRule(val rule:Rule, val compiledRule:CExpressionImpl[Boolean])

case class EvalResult(isSucess:Boolean, result:String, error:String)

object EvalResult {
    implicit val format = Json.format[EvalResult]
}