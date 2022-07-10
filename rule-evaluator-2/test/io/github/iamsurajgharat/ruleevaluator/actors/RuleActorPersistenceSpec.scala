package io.github.iamsurajgharat
package ruleevaluator
package actors

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterEach
import akka.persistence.typed.PersistenceId
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import akka.actor.testkit.typed.scaladsl.LogCapturing
import io.github.iamsurajgharat.ruleevaluator.models.web.RuleMetadata

object RuleActorPersistenceSpec{
  val yourConfiguration = ConfigFactory.defaultApplication().resolve()
  val serializationSettings = EventSourcedBehaviorTestKit.SerializationSettings.enabled.withVerifyState(false)
}

class RuleActorPersistenceSpec 
  extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config.withFallback(RuleActorPersistenceSpec.yourConfiguration))
  with AnyWordSpecLike
  with BeforeAndAfterEach
  with LogCapturing {

    "RuleActor" should {
      "persist events" in {

        import io.github.iamsurajgharat.expressiontree.expressiontree.DataType._
        val persistenceId = PersistenceId.ofUniqueId("RuleActor|shard-0")
        val eventSourcedTestKit = EventSourcedBehaviorTestKit[
                                    RuleActor.Command,
                                    RuleActor.Event,
                                    RuleActor.RuleActorData
                                  ](
                                      system,
                                      RuleActor("rule-shard-0", persistenceId),
                                      RuleActorPersistenceSpec.serializationSettings
                                  )
        // rule to save
        val rule = Rule("id-1", "A > B", "id-1")
        val ruleMetadata = RuleMetadata(Map("A" -> Number, "B" -> Number))

        // save metadata cmd
        val metadataSaveResponse = eventSourcedTestKit.runCommand[
                                          RuleActor.GetMetadataResponse
                                        ](replyTo => RuleActor.SaveMetadataRequest(ruleMetadata, replyTo))

        // assert
        metadataSaveResponse.reply.metadata shouldBe(ruleMetadata)

        // act
        val saveRuleResponse = eventSourcedTestKit.runCommand[
                                          RuleActor.SaveShardRulesResponse
                                        ](replyTo => RuleActor.SaveShardRulesRequest(List(rule), replyTo))

        // assert
        saveRuleResponse.reply.status("id-1") shouldBe Right(())
      }
    }
  }