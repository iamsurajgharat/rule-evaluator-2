package io.github.iamsurajgharat
package ruleevaluator
package services

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import zio.Task
import com.google.inject.ImplementedBy
import zio.ZIO
import java.util.concurrent.TimeUnit
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor
import io.github.iamsurajgharat.ruleevaluator.models.web.SaveRulesResponseDTO
import javax.inject.Inject

@ImplementedBy(classOf[RuleServiceImpl])
trait RuleService {
    def saveRules(rules:List[Rule]):Task[SaveRulesResponseDTO]
}

class RuleServiceImpl @Inject()(
  private val actorSystemService: ActorSystemService,
  private implicit val scheduler:akka.actor.typed.Scheduler) extends RuleService{

  private val ruleManagerActor = actorSystemService.ruleManagerActor
  private var saveReqCnt = 0L;
  override def saveRules(rules: List[Rule]): Task[SaveRulesResponseDTO] = {
    saveReqCnt = saveReqCnt + 1;

    ZIO.fromFuture(implicit ec => {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

      ruleManagerActor.ask(replyTo => RuleManagerActor.SaveRulesRequest(saveReqCnt.toString(), rules, replyTo)).
                  map(r => SaveRulesResponseDTO(r.sucessIds, r.errors))
    })
  }
}