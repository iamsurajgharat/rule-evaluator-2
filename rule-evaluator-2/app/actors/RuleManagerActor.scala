package io.github.iamsurajgharat
package ruleevaluator
package actors

import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import io.github.iamsurajgharat.ruleevaluator.services.ConfService
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.PersistenceId
import play.api.libs.concurrent.ActorModule
import com.google.inject.Provides
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import io.github.iamsurajgharat.ruleevaluator.models.web.RuleMetadata
import io.github.iamsurajgharat.expressiontree.expressiontree.Record
import io.github.iamsurajgharat.ruleevaluator.models.domain.EvalResult
import io.github.iamsurajgharat.ruleevaluator.models.web.EvalConfig

object RuleManagerActor extends ActorModule {
    type Message = Command
    // command / request
    sealed trait Command {
        val cmdId : String
    }

    case class SaveRulesRequest(cmdId:String, rules:List[Rule], replyTo:ActorRef[SaveRulesResponse]) extends Command
    case class SaveMetadataRequest(cmdId:String, metadata:RuleMetadata, replyTo:ActorRef[SaveMetadataResponse]) extends Command
    case class EvaluateRulesRequest(cmdId:String, evalConfig:Option[EvalConfig], records:List[Record], replyTo:ActorRef[EvaluateRulesResponse]) extends Command
    case class GetRulesRequest(cmdId:String, ids:Set[String], replyTo:ActorRef[GetRulesResponse]) extends Command

    // responses
    case class SaveRulesResponse(sucessIds:List[String], errors:Map[String,String])
    case class SaveMetadataResponse(metadata:RuleMetadata)
    case class EvaluateRulesResponse(data:Map[String,List[EvalResult]])
    case class GetRulesResponse(data:List[Rule])

    // events
    sealed trait Event

    @Provides
    def apply(confService:ConfService,
                clusterSharding:ClusterSharding
    ): Behavior[Command] = {
        
        println(Console.BLUE + "Shard region init started" + Console.RESET)
        //val TypeKey = EntityTypeKey[RuleActor.Command]("RuleActor")
        val ruleActorshardRegion: ActorRef[ShardingEnvelope[RuleActor.Command]] =
        clusterSharding.init(Entity(typeKey = RuleActor.TypeKey) { entityContext =>
            RuleActor(entityContext.entityId, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
        })

        println(Console.BLUE + "Shard region init completed" + Console.RESET)

        Behaviors.receive((context, message) => {

            def getShardNumber(id:String) : Int = Math.abs(id.hashCode()) % confService.totalNumberOfShards

            message match {
                case SaveRulesRequest(cmdId, rules, replyTo) => 
                    println(Console.BLUE + "Received msg in manager actor " + Console.RESET)
                    
                    val groups = rules.groupBy(r => getShardNumber(r.id)).toList

                    // Create actor to handle save-shard-rule responses from RuleActors
                    val cActor = context.spawn(saveShardRulesResponseBhr(replyTo, groups.length, Nil, Map.empty[String,String]), "SaveShardRulesResponse-"+cmdId)

                    // send save request to multiple RuleActors as per shardId
                    groups.foreach(ruleGroup => {
                        val entityId = "shard-" + ruleGroup._1
                        val payload = RuleActor.SaveShardRulesRequest(ruleGroup._2, cActor)
                        ruleActorshardRegion ! ShardingEnvelope(entityId, payload)
                    })

                case GetRulesRequest(cmdId, ids, replyTo) => 
                    val groups = ids.groupBy(getShardNumber(_))

                    // Create actor to handle save-shard-rule responses from RuleActors
                    val cActor = context.spawn(getShardRulesResponseBhr(replyTo, groups.size, Nil), "GetShardRulesResponse-"+cmdId)

                    // send save request to multiple RuleActors as per shardId
                    groups.foreach(ruleGroup => {
                        val entityId = "shard-" + ruleGroup._1
                        val payload = RuleActor.GetShardRulesRequest(ruleGroup._2, cActor)
                        ruleActorshardRegion ! ShardingEnvelope(entityId, payload)
                    })
                    println("Made request to "+groups.size + " shard entities")

                case SaveMetadataRequest(cmdId, metadata, replyTo) => 
                    println(Console.BLUE + "Received msg in manager actor to save metadata" + Console.RESET)
                    // Create actor to handle save-shard-rule responses from RuleActors
                    val cActor = context.spawn(saveMetadataResponseHandler(replyTo, confService.totalNumberOfShards), "SaveMetadataResponse-"+cmdId)
                    for(shard <- 0 until confService.totalNumberOfShards) {
                        val entityId = "shard-" + shard
                        val payload = RuleActor.SaveMetadataRequest(metadata, cActor)
                        ruleActorshardRegion ! ShardingEnvelope(entityId, payload)
                    }

                case EvaluateRulesRequest(cmdId, evalConfig, records, replyTo) => 
                    // Create actor to handle save-shard-rule responses from RuleActors
                    val cActor = context.spawn(evaluateRulesResponseHandler(replyTo, confService.totalNumberOfShards, Map.empty), "EvaluateRuleResponse-"+cmdId)
                    for(shard <- 0 until confService.totalNumberOfShards) {
                        val entityId = "shard-" + shard
                        val payload = RuleActor.EvaluateRulesRequest(evalConfig, records, cActor)
                        ruleActorshardRegion ! ShardingEnvelope(entityId, payload)
                    }
            }

            Behaviors.same
        })
    }

    def evaluateRulesResponseHandler(
        replyTo:ActorRef[EvaluateRulesResponse],
        pendingReply:Int,
        response: Map[String, List[EvalResult]]
    ) : Behavior[RuleActor.EvaluateRulesResponse] = Behaviors.receive((_, message) => {
            println(Console.BLUE + s"Received eval response :${pendingReply}" + Console.RESET)
            val newData = message.data.map(x => x._1 -> (if(response.contains(x._1)) response(x._1) ++ x._2 else x._2))
            val mergedResponse = response ++ newData
            // if all responses are received, send collected data to requester, and stop this actor
            if(pendingReply <= 1){
                replyTo ! EvaluateRulesResponse(mergedResponse)
                Behaviors.stopped
            }
            else
                evaluateRulesResponseHandler(replyTo, pendingReply -1, mergedResponse)

    })

    def saveMetadataResponseHandler(
        replyTo:ActorRef[SaveMetadataResponse],
        pendingReply:Int
    ) : Behavior[RuleActor.GetMetadataResponse] = Behaviors.receive((_, message) => {
            // if all responses are received, send collected data to requester, and stop this actor
            if(pendingReply <= 1){
                replyTo ! SaveMetadataResponse(message.metadata)
                Behaviors.stopped
            }
            else
                saveMetadataResponseHandler(replyTo, pendingReply -1)

    })

    def saveShardRulesResponseBhr(
        replyTo:ActorRef[SaveRulesResponse],
        pendingReply:Int,
        sucessIds:List[String],
        errorIds:Map[String,String]
    ) : Behavior[RuleActor.SaveShardRulesResponse] = Behaviors.receive((_, message) => {
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

    def getShardRulesResponseBhr(
        replyTo:ActorRef[GetRulesResponse],
        pendingReply:Int,
        rules:List[Rule]
    ) : Behavior[RuleActor.GetShardRulesResponse] = Behaviors.receive((_, message) => {
            val mergedRules = rules ++ message.data

            println("Received shard response!!")

            // if all responses are received, send collected data to requester, and stop this actor
            if(pendingReply <= 1){
                replyTo ! GetRulesResponse(mergedRules)
                Behaviors.stopped
            }
            else
                getShardRulesResponseBhr(replyTo, pendingReply -1, mergedRules)

    })
}