package io.github.iamsurajgharat.ruleevaluator.controllers

package io.github.iamsurajgharat
package ruleevaluator
package controllers

import akka.http.scaladsl.model.HttpHeader
import org.mockito.ArgumentMatchersSugar
import org.mockito.MockSettings
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.Headers
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers
import zio.ZIO
import _root_.io.github.iamsurajgharat.ruleevaluator.services.RuleService
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.SaveRulesResponseDTO

class RuleControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ArgumentMatchersSugar {
    import play.api.test.Helpers._
    "saveRules" must {
        val ruleServiceMock = mock[RuleService]
        val subject = new RuleController(ruleServiceMock, Helpers.stubControllerComponents())
        "return successIds correctly" in {
            // arrange
            val rules = List(getSampleRule())
            var json = Json.toJson(rules)
            val request = FakeRequest(POST, "api/rules", Headers.create(), json)

            // mock result data
            when(ruleServiceMock.saveRules(any[List[Rule]])).
                thenReturn(
                    ZIO.succeed(SaveRulesResponseDTO(List("id1"), Map.empty))
                )

            // act
            val result = subject.saveRules().apply(request)

            // assure
            status(result) mustBe OK
            val jsValue = Json.parse(contentAsString(result))
            jsValue.validate[SaveRulesResponseDTO] match {
                case JsSuccess(data, _) =>
                    data.successIds.head mustBe "id1"
                case _:JsError => 
                    fail("Invalid json in result ")
            }

            // reset mock
            reset(ruleServiceMock)

        }
    }

    private def getSampleRule() = Rule("id1", "A > B", "id1")
}