package io.github.iamsurajgharat
package ruleevaluator
package actors

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import akka.persistence.typed.scaladsl.Effect
import io.github.iamsurajgharat.ruleevaluator.services.ConfService
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.actor.typed.ActorRef
import java.util.concurrent.TimeUnit
import akka.actor.typed.ActorSystem

object RuleManagerActor {
    // command
    sealed trait Command
    case class SaveRules(rules:List[Rule], replyTo:ActorRef[SaveRulesResponse]) extends Command

    // response
    case class SaveRulesResponse(sucessIds:List[String], errors:Map[String,String])

    // events
    sealed trait Event

    // state
    case class State(n:Int)

    def apply(confService:ConfService,
                ruleActorshardRegion: ActorRef[ShardingEnvelope[RuleActor.Command]],
                theSystem:ActorSystem[_]
            ) : Behavior[Command] = {
        def getShardNumber(id:String) : Int = id.hashCode() % confService.totalNumberOfShards
        def commandHandler(state:State, cmd:Command): Effect[Event, State] = {
            cmd match {
                case SaveRules(rules, replyTo) => 
                    import akka.actor.typed.scaladsl.AskPattern._
                    import akka.util.Timeout
                    
                    implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
                    implicit val system: ActorSystem[_] = theSystem

                    val groupedRules = rules.groupBy(r => getShardNumber(r.id))
                    for(ruleGroup <- groupedRules) {
                        val entityId = "shard-" + ruleGroup._1
                        val response = ruleActorshardRegion.ask[RuleActor.SavingRules](replyTo => {
                            var payload = RuleActor.SaveRules(ruleGroup._2, replyTo)
                            ShardingEnvelope(entityId, payload)
                        })
                    }

                    // No event to persist, just reply back with ack
                    Effect.reply(replyTo)(SaveRulesResponse(Nil, Map.empty[String,String]))
            }
        }

        ???
    }
}