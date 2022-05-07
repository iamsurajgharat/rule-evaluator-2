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

object RuleManagerActor extends ActorModule{
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

    // state
    case class State(saveRequestState:Map[String,SaveRuleRequestState]) {
        def addToSaveRequestState(cmdId:String, saveReqState:SaveRuleRequestState) =
            copy(saveRequestState = saveRequestState + (cmdId -> saveReqState))
        
    }
    case class SaveRuleRequestState(replyTo:ActorRef[SaveRulesResponse], pendingReply:Int, sucessIds:List[String], errorIds:Map[String,String]){
        def this(replyTo:ActorRef[SaveRulesResponse], pendingReply:Int) = {
            this(replyTo, pendingReply, Nil, Map.empty[String,String])
        }
    }

    @Provides
    def apply(confService:ConfService,
                clusterSharding:akka.cluster.sharding.typed.scaladsl.ClusterSharding
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
                import akka.actor.typed.scaladsl.AskPattern._
                import akka.util.Timeout
                
                implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
                //implicit val system: ActorSystem[_] = context.system
                implicit val scheduler = context.system.scheduler
                implicit val ec = context.executionContext
                val groups = rules.groupBy(r => getShardNumber(r.id)).toList
                val cActor = context.spawn(saveShardRulesResponseBhr(replyTo, groups.length, Nil, Map.empty[String,String]), "SaveRulesResponse-"+cmdId)
                groups.foreach(ruleGroup => {
                    val entityId = "shard-" + ruleGroup._1
                    var payload = RuleActor.SaveShardRulesRequest(ruleGroup._2, cActor)
                    ruleActorshardRegion ! ShardingEnvelope(entityId, payload)
                    //groupCount = groupCount + 1
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
            if(pendingReply <= 1){
                replyTo ! SaveRulesResponse(newSuccessIds, newErrorIds)
                Behaviors.stopped
            }
            else
                saveShardRulesResponseBhr(replyTo, pendingReply -1, newSuccessIds, newErrorIds)

    })
}