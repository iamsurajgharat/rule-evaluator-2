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
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.PersistenceId
import play.api.libs.concurrent.ActorModule
import com.google.inject.Provides
import akka.cluster.sharding.typed.scaladsl.ClusterSharding

object RuleManagerActor extends ActorModule {
    type Message = Command
    // command / request
    sealed trait Command {
        val cmdId : String
    }

    case class SaveRulesRequest(cmdId:String, rules:List[Rule], replyTo:ActorRef[SaveRulesResponse]) extends Command

    // responses
    case class SaveRulesResponse(sucessIds:List[String], errors:Map[String,String])

    // events
    sealed trait Event

    @Provides
    def apply(confService:ConfService,
                clusterSharding:ClusterSharding
    ): Behavior[Command] = Behaviors.receive((context, message) => {

        val TypeKey = EntityTypeKey[RuleActor.Command]("RuleActor")
        val ruleActorshardRegion: ActorRef[ShardingEnvelope[RuleActor.Command]] =
        clusterSharding.init(Entity(typeKey = RuleActor.TypeKey) { entityContext =>
            RuleActor(entityContext.entityId, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
        })
        
        def getShardNumber(id:String) : Int = id.hashCode() % confService.totalNumberOfShards

        message match {
            case SaveRulesRequest(cmdId, rules, replyTo) => 
                println(Console.BLUE + "Received msg in manager actor " + Console.RESET)
                
                val groups = rules.groupBy(r => getShardNumber(r.id)).toList

                // Create actor to handle save-shard-rule responses from RuleActors
                val cActor = context.spawn(saveShardRulesResponseBhr(replyTo, groups.length, Nil, Map.empty[String,String]), "SaveRulesResponse-"+cmdId)

                // send save request to multiple RuleActors as per shardId
                groups.foreach(ruleGroup => {
                    val entityId = "shard-" + ruleGroup._1
                    var payload = RuleActor.SaveShardRulesRequest(ruleGroup._2, cActor)
                    ruleActorshardRegion ! ShardingEnvelope(entityId, payload)
                })
        }

        Behaviors.same
    })

    def saveShardRulesResponseBhr(
        replyTo:ActorRef[SaveRulesResponse],
        pendingReply:Int,
        sucessIds:List[String],
        errorIds:Map[String,String]
    ) : Behavior[RuleActor.SaveShardRulesResponse] = Behaviors.receive((context, message) => {
            val (s, e) = message.status.partition(_._2.isRight)
            val newSuccessIds = sucessIds.concat(s.keySet).toList
            val newErrorIds = errorIds ++ e.map(x => x._1 -> x._2.left.toOption.get)

            // if all responses are received, send collected data to requester, and stop this actor
            if(pendingReply <= 1){
                replyTo ! SaveRulesResponse(newSuccessIds, newErrorIds)
                Behaviors.stopped
            }
            else
                saveShardRulesResponseBhr(replyTo, pendingReply -1, newSuccessIds, newErrorIds)

    })
}