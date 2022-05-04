package io.github.iamsurajgharat
package ruleevaluator
package models.store

import io.github.iamsurajgharat.expressiontree.SExpression
import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.expressiontree.expressiontree.CExpressionImpl

class SRule(id:String, exprStr:String, result:String, sexpr:SExpression) {
    def toRule() = new Rule(id, exprStr, result)
    def toCRule() = sexpr.compile().asInstanceOf[CExpressionImpl[Boolean]]
}