package io.github.iamsurajgharat
package ruleevaluator
package loaders

//import akka.actor.typed.ActorSystem
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import javax.inject._
import play.api.inject.ApplicationLifecycle
import akka.actor.ActorSystem
import scala.concurrent.Future
import play.api.Configuration
import play.api.ConfigLoader.stringLoader

class AkkaBootstrapLoader @Inject() (actorSystem: ActorSystem, config: Configuration, lifecycle: ApplicationLifecycle) {
    
    if(config.get("runtime.mode") == "k8s") {
      println("Starting akka bootstrap")
    
      // Akka Management hosts the HTTP routes used by bootstrap
      AkkaManagement(actorSystem).start()

      // Starting the bootstrap process needs to be done explicitly
      ClusterBootstrap(actorSystem).start()

      println("Done with starting akka bootstrap")
    }
    
    // Shut-down hook
    lifecycle.addStopHook { () =>
      Future.successful(())
    }
}