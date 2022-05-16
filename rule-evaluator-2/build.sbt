name := """rule-evaluator-2"""
organization := "com.surajgharat"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(Antlr4Plugin)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "io.github.iamsurajgharat" %% "expression-tree" % "1.0.2"
libraryDependencies += "dev.zio" %% "zio" % "1.0.12"

val AkkaVersion = "2.6.18"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion
libraryDependencies += clusterSharding
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.5"



// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.surajgharat.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.surajgharat.binders._"

antlr4PackageName in Antlr4 := Some("io.github.iamsurajgharat.ruleevaluator.antlr4")
antlr4GenVisitor in Antlr4 := true // default: false