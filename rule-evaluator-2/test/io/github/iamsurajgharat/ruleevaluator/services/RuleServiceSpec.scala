package io.github.iamsurajgharat
package ruleevaluator
package services

import org.mockito.ArgumentMatchersSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.mockito.MockitoSugar
import org.mockito.scalatest.ResetMocksAfterEachTest
import io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import org.scalatestplus.play.PlaySpec
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor
import akka.actor.typed.scaladsl.Behaviors
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor.SaveRulesRequest
import org.mockito.IdiomaticMockito._
import helpers.ZIOHelper._
import io.github.iamsurajgharat.ruleevaluator.models.web.SaveRulesResponseDTO
import java.util.concurrent.TimeoutException

class RuleServiceSpec
    extends PlaySpec
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  val testKit = ActorTestKit()
  val actorSystemServiceMock = mock[ActorSystemService]
  //val subject = new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)

  "saveRules" must {
    "return success response for valid input" in {
      // arrange
      val rules = List(Rule("r-1", "A > B", "r-1"))

      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { msg =>
        msg match {
          case SaveRulesRequest(_, _, replyTo) =>
            replyTo ! RuleManagerActor.SaveRulesResponse(List("r-1"), Map.empty[String, String])
          case _ => ???
        }
        Behaviors.same
      }

      val ruleManagerActorProbe = testKit.createTestProbe[RuleManagerActor.Command]()
      val ruleManagerActor = testKit.spawn(Behaviors.monitor(ruleManagerActorProbe.ref, ruleManagerActorBehaviour))

      
      actorSystemServiceMock.ruleManagerActor returns ruleManagerActor
      val subject = new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)

      // act
      val result = subject.saveRules(rules)

      // assert
      interpret(result) mustBe SaveRulesResponseDTO(List("r-1"), Map.empty[String,String])
    }

    "throw TimeoutException when actor does not respond" in {
      // arrange
      val rules = List(Rule("r-1", "A > B", "r-1"))

      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { _ =>
        Behaviors.same
      }

      val ruleManagerActorProbe = testKit.createTestProbe[RuleManagerActor.Command]()
      val ruleManagerActor = testKit.spawn(Behaviors.monitor(ruleManagerActorProbe.ref, ruleManagerActorBehaviour))

      
      actorSystemServiceMock.ruleManagerActor returns ruleManagerActor
      val subject = new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)

      // act and assert
      an [TimeoutException] should be thrownBy interpret(subject.saveRules(rules))
    }
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
}
