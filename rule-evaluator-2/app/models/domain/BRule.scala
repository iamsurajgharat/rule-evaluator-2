package io.github.iamsurajgharat
package ruleevaluator
package models.domain

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.expressiontree.expressiontree.CExpressionImpl

class BRule(val rule:Rule, val compiledRule:CExpressionImpl[Boolean])