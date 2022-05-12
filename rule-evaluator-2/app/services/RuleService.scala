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
import io.github.iamsurajgharat.ruleevaluator.models.web.GetRulesResponseDTO
import io.github.iamsurajgharat.expressiontree.expressiontree.Record
import io.github.iamsurajgharat.ruleevaluator.models.web.EvaluateRulesResponseDTO
import io.github.iamsurajgharat.ruleevaluator.models.web.EvaluateRulesRequestDTO
import io.github.iamsurajgharat.ruleevaluator.models.web.SaveConfigAndMetadataRequestDTO
import io.github.iamsurajgharat.ruleevaluator.models.web.SaveConfigAndMetadataResponseDTO

@ImplementedBy(classOf[RuleServiceImpl])
trait RuleService {
    def saveRules(rules:List[Rule]):Task[SaveRulesResponseDTO]
    def getRules(ids: Set[String]): Task[GetRulesResponseDTO]
    def evalRules(request: EvaluateRulesRequestDTO): Task[EvaluateRulesResponseDTO]
    def saveConfigAndMetadata(request:SaveConfigAndMetadataRequestDTO) : Task[SaveConfigAndMetadataResponseDTO]
}

class RuleServiceImpl @Inject()(
  private val actorSystemService: ActorSystemService,
  private implicit val scheduler:akka.actor.typed.Scheduler) extends RuleService{

  private val ruleManagerActor = actorSystemService.ruleManagerActor
  private var saveReqCnt = 0L;
  private var saveMetaReqCnt = 0L;
  private var getReqCnt = 0L;
  private var evalReqCnt = 0L;
  override def saveRules(rules: List[Rule]): Task[SaveRulesResponseDTO] = {
    saveReqCnt = saveReqCnt + 1;

    ZIO.fromFuture(implicit ec => {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

      ruleManagerActor
        .ask(replyTo => RuleManagerActor.SaveRulesRequest(saveReqCnt.toString(), rules, replyTo))
        .map(r => SaveRulesResponseDTO(r.sucessIds, r.errors))
    })
  }

  override def saveConfigAndMetadata(request:SaveConfigAndMetadataRequestDTO): Task[SaveConfigAndMetadataResponseDTO] = {
    saveMetaReqCnt = saveMetaReqCnt + 1;

    ZIO.fromFuture(implicit ec => {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

      println("Sending save-metadata msg to manager")

      ruleManagerActor
        .ask(replyTo => RuleManagerActor.SaveMetadataRequest(saveMetaReqCnt.toString(), request.metadata, replyTo))
        .map(r => SaveConfigAndMetadataResponseDTO(r.metadata))
    })
  }

  override def getRules(ids: Set[String]): Task[GetRulesResponseDTO] = {
    getReqCnt = getReqCnt + 1;

    ZIO.fromFuture(implicit ec => {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

      ruleManagerActor
        .ask(replyTo => RuleManagerActor.GetRulesRequest(getReqCnt.toString(), ids, replyTo))
        .map(x => GetRulesResponseDTO(x.data))
    })
  }

  override def evalRules(request: EvaluateRulesRequestDTO): Task[EvaluateRulesResponseDTO] = {
    evalReqCnt = evalReqCnt + 1;

    ZIO.fromFuture(implicit ec => {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

      ruleManagerActor
        .ask(replyTo => RuleManagerActor.EvaluateRulesRequest(evalReqCnt.toString(), request.context, request.records, replyTo))
        .map(x => EvaluateRulesResponseDTO(x.data))
    })
  }
}