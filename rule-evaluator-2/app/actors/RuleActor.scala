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

    sealed trait Command extends CborSerializable
    final case class SaveRules(rules:List[Rule], replyTo:ActorRef[SavingRules]) extends Command
    final case class GetRules(ids:List[String], replyTo:ActorRef[RequestedRules]) extends Command

    // responses
    final case class SavingRules(id:List[String])
    final case class RequestedRules(data:List[Rule])

    // events
    sealed trait Event
    final case class SavedRules(rules:List[SRule]) extends Event

    // state
    final case class RuleActorData(private val rules:Map[String,BRule]){
        def saveRules(newRules:List[BRule]) : RuleActorData = {
            ???
        }

        def getRulesDTO():List[Rule] = {
            rules.map(x => x._2.rule).toList
        }
    }

    private def commandHandler(state:RuleActorData, cmd:Command):Effect[Event, RuleActorData] = {
        cmd match {
            case GetRules(ids, replyTo) => 
                replyTo ! RequestedRules(state.getRulesDTO())
                Effect.none
            case SaveRules(rules, replyTo) => 
                Effect.persist(SavedRules(rules.map(getSavedRule).toList))
        }
    }

    private def eventHandler(state:RuleActorData, evt:Event):RuleActorData = {
        evt match {
            case SavedRules(rules) => state.saveRules(rules.map(getBRule).toList)
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