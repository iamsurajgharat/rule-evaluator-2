name := "rule-evaluator-2"
organization := "com.surajgharat"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(Antlr4Plugin)
  .enablePlugins(AshScriptPlugin)

scalaVersion := "2.13.8"
val AkkaVersion = "2.6.18"
val AkkaManagementVersion = "1.1.3"

libraryDependencies ++= Seq(
  "io.github.iamsurajgharat" %% "expression-tree" % "1.0.2",
  "dev.zio" %% "zio" % "1.0.12",

  // akka
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  clusterSharding,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,

  // akka cluster formation
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % AkkaManagementVersion,

  // akka persistence
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.2",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",

  // unit testing
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  //"org.scalatest" %% "scalatest" % "3.1.4" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.6",
  "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test
)

scalacOptions ++= Seq(
  "-Ywarn-unused",
  "-deprecation",
  "Ylog-classpath"
)

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

antlr4PackageName in Antlr4 := Some("io.github.iamsurajgharat.ruleevaluator.antlr4")
antlr4GenVisitor in Antlr4 := true // default: false

ThisBuild / dynverSeparator := "-"

import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

Docker / maintainer := "mr.surajgharat2@gmail.com"
Docker / packageName := "surajgharat/rule-eval-main-service"
Docker / version := sys.env.getOrElse("BUILD_NUMBER", "6")
Docker / daemonUserUid := None
Docker / daemonUser := "daemon"
dockerExposedPorts := Seq(9000)
//dockerBaseImage := "openjdk:11.0.15-oracle"
dockerBaseImage := "adoptopenjdk:11-jre-hotspot"
dockerUpdateLatest := true

// run command settings
PlayKeys.devSettings += "runtime.mode" -> "Local"

//jacocoIncludes := Seq("**/*")
//jacocoIncludes ++= Seq("io.github.iamsurajgharat.ruleevaluator/**")