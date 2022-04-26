name := """rule-evaluator-2"""
organization := "com.surajgharat"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(Antlr4Plugin)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "io.github.iamsurajgharat" %% "expression-tree" % "0.0.1"
libraryDependencies += "dev.zio" %% "zio" % "1.0.12"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.surajgharat.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.surajgharat.binders._"

antlr4PackageName in Antlr4 := Some("io.github.iamsurajgharat.ruleevaluator.antlr4")