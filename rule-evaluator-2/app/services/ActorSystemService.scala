package io.github.iamsurajgharat
package ruleevaluator
package services

import akka.actor.typed.ActorRef
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor
import com.google.inject.ImplementedBy
import javax.inject.Inject

@ImplementedBy(classOf[ActorSystemServiceImpl])
trait ActorSystemService{
    val ruleManagerActor : ActorRef[RuleManagerActor.Command]
}

class ActorSystemServiceImpl @Inject()(val ruleManagerActor:ActorRef[RuleManagerActor.Command]) extends ActorSystemService {
}


