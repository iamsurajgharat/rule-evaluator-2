package io.github.iamsurajgharat
package ruleevaluator
package services

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import zio.Task
import com.google.inject.ImplementedBy
import zio.ZIO

@ImplementedBy(classOf[RuleServiceImpl])
trait RuleService {
    def saveRules(rules:List[Rule]):Task[Unit]
}

class RuleServiceImpl() extends RuleService{

  override def saveRules(rules: List[Rule]): Task[Unit] = {
    println("I am in rule service")
    Task.fromTry(scala.util.Try(???))
    ZIO.succeed(())
  }

    
}