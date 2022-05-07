package io.github.iamsurajgharat
package ruleevaluator
package modules


import com.google.inject.AbstractModule
import com.google.inject.name.Names
import akka.actor.typed.ActorSystem
import io.github.iamsurajgharat.ruleevaluator.services.ConfServiceImpl
import play.api.libs.concurrent.AkkaGuiceSupport
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor
import akka.cluster.sharding.typed.scaladsl.ClusterSharding

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    
    val confService = new ConfServiceImpl()

    bindTypedActor(RuleManagerActor, "RuleManagerActor")

    println(Console.RED + " done with binding " + Console.RESET)
  }
}
