package io.github.iamsurajgharat
package ruleevaluator
package services

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import zio.Task
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[RuleServiceImpl])
trait RuleService {
    def saveRules(rules:List[Rule]):Task[List[Rule]]
}

class RuleServiceImpl(n1:Int) extends RuleService{

  override def saveRules(rules: List[Rule]): Task[List[Rule]] = ???

    
}