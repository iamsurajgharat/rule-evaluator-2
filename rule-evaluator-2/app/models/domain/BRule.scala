package models.domain

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.expressiontree.expressiontree.CExpressionImpl

class BRule(rule:Rule, compiledRule:CExpressionImpl[Boolean])