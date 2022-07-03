package io.github.iamsurajgharat
package ruleevaluator
package modules


import com.google.inject.AbstractModule
import io.github.iamsurajgharat.ruleevaluator.services.ConfServiceImpl
import play.api.libs.concurrent.AkkaGuiceSupport
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor
import io.github.iamsurajgharat.ruleevaluator.loaders.AkkaBootstrapLoader

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    
    new ConfServiceImpl()

    bindTypedActor(RuleManagerActor, "RuleManagerActor")

    //bind(classOf[AkkaBootstrapLoader]).asEagerSingleton()
    bind(classOf[AkkaBootstrapLoader]).asEagerSingleton()

    println(Console.RED + " done with binding " + Console.RESET)
    
  }
}
