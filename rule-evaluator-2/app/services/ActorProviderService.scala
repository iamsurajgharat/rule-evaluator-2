package io.github.iamsurajgharat
package ruleevaluator
package services

import akka.actor.typed.ActorRef
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ActorProviderServiceImpl])
trait ActorProviderService{
    def getRuleManagerActor() : ActorRef[RuleManagerActor.Command]
}

class ActorProviderServiceImpl() extends ActorProviderService {
    def getRuleManagerActor(): ActorRef[Any] = ???
}


