package io.github.iamsurajgharat
package ruleevaluator
package actors

import io.github.iamsurajgharat.ruleevaluator.models.store.SRule
import akka.actor.typed.ActorRef
import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.ruleevaluator.models.domain.BRule
import akka.persistence.typed.scaladsl.Effect
import io.github.iamsurajgharat.expressiontree.SExpression
import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.scaladsl.EventSourcedBehavior

object RuleActor {
    // commands
    // save-rules
    // get-rules
    // evaluate against rules
    // delete rules

    sealed trait Command
    final case class SaveShardRulesRequest(rules:List[Rule], replyTo:ActorRef[SaveShardRulesResponse]) extends Command
    final case class GetShardRulesRequest(ids:Set[String], replyTo:ActorRef[GetShardRulesResponse]) extends Command

    // responses
    final case class SaveShardRulesResponse(status:Map[String,Either[String,Unit]])
    final case class GetShardRulesResponse(data:List[Rule])

    // events
    sealed trait Event
    final case class SavedShardRules(rules:List[SRule]) extends Event

    // state
    final case class RuleActorData(private val rules:Map[String,BRule]){
        def saveRules(newRules:List[BRule]) : RuleActorData = {
            val mergedRules = rules ++ newRules.map(x => x.rule.id -> x).toMap
            copy(rules = mergedRules)
        }

        def getRulesDTO(ids:Set[String]) = ids.withFilter(rules.contains(_)).map(rules(_).rule).toList
    }

    private def commandHandler(state:RuleActorData, cmd:Command):Effect[Event, RuleActorData] = {
        cmd match {
            case GetShardRulesRequest(ids, replyTo) => 
                Effect.none.thenReply(replyTo)(state => GetShardRulesResponse(state.getRulesDTO(ids)))
            case SaveShardRulesRequest(rules, replyTo) => 
                println(Console.BLUE + "Received msg in rule actor " + Console.RESET)
                Effect.persist(SavedShardRules(rules.map(getSavedRule).toList)).
                thenReply(replyTo)(st => SaveShardRulesResponse(rules.map(x => x.id -> Right(())).toMap))
        }
    }

    private def eventHandler(state:RuleActorData, evt:Event):RuleActorData = {
        evt match {
            case SavedShardRules(rules) => state.saveRules(rules.map(getBRule).toList)
        }
    }

    private def getSavedRule(rule:Rule):SRule = 
        new SRule(rule.id, rule.expression, rule.result, getSExpression(rule.expression))

    private def getBRule(srule:SRule):BRule = new BRule(srule.toRule(), srule.toCRule())
    
    private def getSExpression(expr:String):SExpression = SExpression.constant(true)

    val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Rule")

    def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] = {
        Behaviors.setup { context =>
            context.log.info("Starting RuleActor {}", entityId)
            EventSourcedBehavior(
                persistenceId, 
                emptyState = RuleActorData(Map.empty[String,BRule]), 
                commandHandler, 
                eventHandler
            )
        }
    }
}