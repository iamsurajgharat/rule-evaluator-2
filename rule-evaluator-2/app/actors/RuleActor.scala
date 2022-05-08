package io.github.iamsurajgharat
package ruleevaluator
package actors

import io.github.iamsurajgharat.ruleevaluator.models.store.SRule
import akka.actor.typed.ActorRef
import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.ruleevaluator.models.web.RuleMetadata
import io.github.iamsurajgharat.ruleevaluator.models.domain.BRule
import akka.persistence.typed.scaladsl.Effect
import io.github.iamsurajgharat.expressiontree.SExpression
import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import io.github.iamsurajgharat.ruleevaluator.parsers.RuleVisitor
import org.antlr.v4.runtime.ANTLRInputStream
import io.github.iamsurajgharat.ruleevaluator.antlr4.RuleLexer
import org.antlr.v4.runtime.CommonTokenStream
import io.github.iamsurajgharat.ruleevaluator.antlr4.RuleParser
import io.github.iamsurajgharat.expressiontree.expressiontree.Record
import io.github.iamsurajgharat.ruleevaluator.models.domain.EvalResult

object RuleActor {
    // commands
    // save-rules
    // get-rules
    // evaluate against rules
    // delete rules

    sealed trait Command
    final case class SaveShardRulesRequest(rules:List[Rule], replyTo:ActorRef[SaveShardRulesResponse]) extends Command
    final case class GetShardRulesRequest(ids:Set[String], replyTo:ActorRef[GetShardRulesResponse]) extends Command
    final case class SaveMetadataRequest(metadata:RuleMetadata, replyTo:ActorRef[GetMetadataResponse]) extends Command
    final case class GetMetadataRequest(replyTo:ActorRef[GetMetadataResponse]) extends Command
    final case class EvaluateRulesRequest(records:List[Record], replyTo:ActorRef[EvaluateRulesResponse]) extends Command

    // responses
    final case class SaveShardRulesResponse(status:Map[String,Either[String,Unit]])
    final case class GetMetadataResponse(metadata:RuleMetadata)
    final case class GetShardRulesResponse(data:List[Rule])
    final case class EvaluateRulesResponse(data:Map[String,EvalResult])

    // events
    sealed trait Event
    final case class SavedShardRules(rules:List[SRule]) extends Event
    final case class SavedMetadata(mdata:RuleMetadata) extends Event

    // state
    final case class RuleActorData(val visitor: RuleVisitor, val rules:Map[String,BRule]){
        def saveRules(newRules:List[BRule]) : RuleActorData = {
            val mergedRules = rules ++ newRules.map(x => x.rule.id -> x).toMap
            copy(rules = mergedRules)
        }

        def getRulesDTO(ids:Set[String]) = ids.withFilter(rules.contains(_)).map(rules(_).rule).toList
    }

    val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Rule")

    def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] = {
        Behaviors.setup { context =>
            context.log.info("Starting RuleActor {}", entityId)
            EventSourcedBehavior(
                persistenceId, 
                emptyState = RuleActorData(new RuleVisitor(RuleMetadata.empty), Map.empty[String,BRule]), 
                commandHandler, 
                eventHandler
            )
        }
    }

    private def commandHandler(state:RuleActorData, cmd:Command):Effect[Event, RuleActorData] = {
        cmd match {
            case GetShardRulesRequest(ids, replyTo) => 
                Effect
                    .none
                    .thenReply(replyTo)(state => GetShardRulesResponse(state.getRulesDTO(ids)))

            case SaveShardRulesRequest(rules, replyTo) => 
                println(Console.BLUE + "Received msg in rule actor " + Console.RESET)
                Effect
                    .persist(SavedShardRules(rules.map(r => getSavedRule(r, state.visitor)).toList))
                    .thenReply(replyTo)(st => SaveShardRulesResponse(rules.map(x => x.id -> Right(())).toMap))

            case SaveMetadataRequest(metadata, replyTo) => 
                Effect
                    .persist(SavedMetadata(state.visitor.metadata ++ metadata))
                    .thenReply(replyTo)(s => GetMetadataResponse(s.visitor.metadata))

            case GetMetadataRequest(replyTo) => 
                Effect.reply(replyTo)(GetMetadataResponse(state.visitor.metadata))
        }
    }

    private def eventHandler(state:RuleActorData, evt:Event):RuleActorData = {
        evt match {
            case SavedShardRules(rules) => state.saveRules(rules.map(getBRule).toList)
            case SavedMetadata(mdata) => state.copy(visitor = new RuleVisitor(mdata))
        }
    }

    private def getSavedRule(rule:Rule, visitor: RuleVisitor):SRule = 
        new SRule(rule.id, rule.expression, rule.result, getSExpression(rule.expression, visitor))

    private def getBRule(srule:SRule):BRule = new BRule(srule.toRule(), srule.toCRule())
    
    private def getSExpression(expr:String, visitor: RuleVisitor):SExpression = {
        val input = new ANTLRInputStream(expr)
        val lexer = new RuleLexer(input)
        val tokens = new CommonTokenStream(lexer)
        val parser = new RuleParser(tokens)
        val tree = parser.expr()
        visitor.visit(tree)
    }
}