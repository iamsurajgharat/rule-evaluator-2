package io.github.iamsurajgharat
package ruleevaluator
package services

import org.mockito.ArgumentMatchersSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.mockito.MockitoSugar
import org.mockito.scalatest.ResetMocksAfterEachTest
import akka.actor.typed.ActorRef
import io.github.iamsurajgharat.ruleevaluator.actors.RuleManagerActor

class ActorSystemServiceImplSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

        val ruleManagerActorMock = mock[ActorRef[RuleManagerActor.Command]]
        val subject = new ActorSystemServiceImpl(ruleManagerActorMock)
        
        "ruleManagerActor" must {
            "always return correct instance" in {

                // act
                val result = subject.ruleManagerActor

                // assert
                result mustBe ruleManagerActorMock
            }
        }
    }
