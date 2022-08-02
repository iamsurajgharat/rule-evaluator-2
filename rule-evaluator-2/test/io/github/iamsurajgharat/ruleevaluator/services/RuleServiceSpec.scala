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
import io.github.iamsurajgharat.ruleevaluator.models.web._
import java.util.concurrent.TimeoutException
import io.github.iamsurajgharat.expressiontree.expressiontree._
import io.github.iamsurajgharat.ruleevaluator.models.domain.EvalResult

class RuleServiceSpec
    extends PlaySpec
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  val testKit = ActorTestKit()
  val actorSystemServiceMock = mock[ActorSystemService]
  // val subject = new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)

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
      interpret(result) mustBe SaveRulesResponseDTO(List("r-1"), Map.empty[String, String])
    }

    "throw TimeoutException when actor does not respond" in {
      // arrange
      val rules = List(Rule("r-1", "A > B", "r-1"))

      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { _ =>
        // do not reply to message to produce the TimeoutException
        Behaviors.same
      }

      val ruleManagerActorProbe = testKit.createTestProbe[RuleManagerActor.Command]()
      val ruleManagerActor = testKit.spawn(Behaviors.monitor(ruleManagerActorProbe.ref, ruleManagerActorBehaviour))

      actorSystemServiceMock.ruleManagerActor returns ruleManagerActor
      val subject = new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)

      // act and assert
      an[TimeoutException] must be thrownBy interpret(subject.saveRules(rules))
    }

    "send correct counter in RuleManagerActor message" in {
      // arrange
      val rules = List(Rule("r-1", "A > B", "r-1"))

      var capturedCounter: Option[String] = None
      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { msg =>
        msg match {
          case SaveRulesRequest(cmdId, _, replyTo) =>
            capturedCounter = Some(cmdId)
            replyTo ! RuleManagerActor.SaveRulesResponse(List("r-1"), Map.empty[String, String])
          case _ => ???
        }
        Behaviors.same
      }

      val ruleManagerActorProbe = testKit.createTestProbe[RuleManagerActor.Command]()
      val ruleManagerActor = testKit.spawn(Behaviors.monitor(ruleManagerActorProbe.ref, ruleManagerActorBehaviour))

      actorSystemServiceMock.ruleManagerActor returns ruleManagerActor
      val subject = new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)

      // act - 2 times
      interpret(subject.saveRules(rules))
      interpret(subject.saveRules(rules))

      // assert
      capturedCounter mustBe Some("2")
    }
  }

  "saveConfigAndMetadata" must {
    "return success response for valid input" in {
      // arrange
      val metadata = RuleMetadata(Map("A" -> DataType.Number, "B" -> DataType.Number))
      val request = SaveConfigAndMetadataRequestDTO(metadata)

      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { msg =>
        msg match {
          case RuleManagerActor.SaveMetadataRequest(_, _, replyTo) =>
            replyTo ! RuleManagerActor.SaveMetadataResponse(metadata)
          case _ => ???
        }
        Behaviors.same
      }

      val subject = createTestSubject(ruleManagerActorBehaviour)

      // act
      val result = subject.saveConfigAndMetadata(request)

      // assert
      interpret(result) mustBe SaveConfigAndMetadataResponseDTO(metadata)
    }
  }

  "getRules" must {
    "return success response for valid input" in {
      // arrange
      val request = Set("rule-1", "rule-2")
      val response = List(Rule("rule-1", "A > B", "result-1"))

      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { msg =>
        msg match {
          case RuleManagerActor.GetRulesRequest(_, _, replyTo) =>
            replyTo ! RuleManagerActor.GetRulesResponse(response)
          case _ => ???
        }
        Behaviors.same
      }

      val subject = createTestSubject(ruleManagerActorBehaviour)

      // act
      val result = interpret(subject.getRules(request))

      // assert
      result mustBe GetRulesResponseDTO(response)
    }
  }

  "evalRules" must {
    "return success response for valid input" in {
      // arrange
      val records = List(new RecordImpl(Map("field1" -> RText("Value1"))))
      val request = EvaluateRulesRequestDTO(None, records)
      val response = Map("record-1" -> List(EvalResult(true, "rule-1", "")))

      // mock result data
      val ruleManagerActorBehaviour = Behaviors.receiveMessage[RuleManagerActor.Command] { msg =>
        msg match {
          case RuleManagerActor.EvaluateRulesRequest(_, _, _, replyTo) =>
            replyTo ! RuleManagerActor.EvaluateRulesResponse(response)
          case _ => ???
        }
        Behaviors.same
      }

      val subject = createTestSubject(ruleManagerActorBehaviour)

      // act
      val result = interpret(subject.evalRules(request))

      // assert
      result mustBe EvaluateRulesResponseDTO(response)
    }
  }

  private def createTestSubject(
      ruleManagerActorBehaviour: Behaviors.Receive[RuleManagerActor.Command]
  ): RuleServiceImpl = {
    val ruleManagerActorProbe = testKit.createTestProbe[RuleManagerActor.Command]()
    val ruleManagerActor = testKit.spawn(Behaviors.monitor(ruleManagerActorProbe.ref, ruleManagerActorBehaviour))
    actorSystemServiceMock.ruleManagerActor returns ruleManagerActor

    return new RuleServiceImpl(actorSystemServiceMock, testKit.scheduler)
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
}
