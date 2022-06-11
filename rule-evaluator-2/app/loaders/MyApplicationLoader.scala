package io.github.iamsurajgharat
package ruleevaluator
package loaders

import play.api._
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.api.inject.guice.GuiceApplicationLoader
import play.api.inject.guice.GuiceApplicationBuilder
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap

class MyApplicationLoader extends GuiceApplicationLoader {
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    new MyComponents(context).initAkkaCluster()
    initialBuilder
  }
}

class MyComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents {  
  def initAkkaCluster():Unit = {
    println("Starting akka bootstrap 222")
    
    // Akka Management hosts the HTTP routes used by bootstrap
    AkkaManagement(actorSystem).start()

    // Starting the bootstrap process needs to be done explicitly
    ClusterBootstrap(actorSystem).start()

    println("Done with starting akka bootstrap 222")
  }
  lazy val router = Router.empty
}