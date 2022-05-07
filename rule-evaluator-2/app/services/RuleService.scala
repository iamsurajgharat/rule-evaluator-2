package io.github.iamsurajgharat
package ruleevaluator
package services

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import zio.Task
import com.google.inject.ImplementedBy
import zio.ZIO
import java.util.concurrent.TimeUnit
import akka.actor.typed.ActorSystem
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
  private var saveReqCnt = 0l;
  override def saveRules(rules: List[Rule]): Task[SaveRulesResponseDTO] = {
    import akka.actor.typed.scaladsl.AskPattern._
    import akka.util.Timeout
    
    
    implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
    //implicit val system: ActorSystem[_] = context.system
    //implicit val scheduler = actorSystemService.actorSystem.scheduler
    //implicit val ec = actorSystemService.actorSystem.executionContext

    // val result = ruleManagerActor.flatMap(rm => rm.ask(replyTo => RuleManagerActor.SaveRulesRequest("c1", rules, replyTo))).
    //               map(r => SaveRulesResponseDTO(r.sucessIds, r.errors))

    saveReqCnt = saveReqCnt + 1;

    ZIO.fromFuture(implicit ec => {
      ruleManagerActor.ask(replyTo => RuleManagerActor.SaveRulesRequest(saveReqCnt.toString(), rules, replyTo)).
                  map(r => SaveRulesResponseDTO(r.sucessIds, r.errors))
    })
  }

    
}