package io.github.iamsurajgharat
package ruleevaluator
package helpers

import io.github.iamsurajgharat.ruleevaluator.actors.RuleActor
import play.api.libs.json.Json
import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.serialization.SerializerWithStringManifest
import akka.actor.typed.ActorSystem
import javax.inject.Inject
import akka.actor.typed.scaladsl.adapter._

class PlayJsonSerializer @Inject()(system: ExtendedActorSystem) extends SerializerWithStringManifest{
    private val actorRefResolver:ActorRefResolver = ActorRefResolver(system.toTyped)
    private val EvalReq = "eval-req"

    override def identifier = 98765422

    override def manifest(msg: AnyRef) = msg match {
        case _: RuleActor.EvaluateRulesRequest => EvalReq
        case _ =>
            throw new IllegalArgumentException(s"Can't serialize object of type ${msg.getClass} in [${getClass.getName}]")
    }

    // "toBinary" serializes the given object to an Array of Bytes
    def toBinary(obj: AnyRef): Array[Byte] = obj match {
        case req:RuleActor.EvaluateRulesRequest => 
            val jsValue = Json.toJson(req)(RuleActor.EvaluateRulesRequest.getWrites(actorRefResolver))
            Json.toBytes(jsValue)
        case _ =>
            throw new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass} in [${getClass.getName}]")
    }

    override def fromBinary(bytes: Array[Byte], manifest: String) = {
        manifest match {
        case EvalReq =>
            Json.parse(bytes)
                .as[RuleActor.EvaluateRulesRequest](RuleActor.EvaluateRulesRequest.getReads(actorRefResolver))
        case _ =>
            throw new IllegalArgumentException(s"Unknown manifest [$manifest]")
        }
    }
}