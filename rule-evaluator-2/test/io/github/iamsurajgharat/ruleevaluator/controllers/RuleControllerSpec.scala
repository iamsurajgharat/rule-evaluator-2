package io.github.iamsurajgharat
package ruleevaluator
package controllers

import org.mockito.ArgumentMatchersSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers
import _root_.io.github.iamsurajgharat.ruleevaluator.services.RuleService
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.Rule
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.SaveRulesResponseDTO
import org.mockito.MockitoSugar
import zio.ZIO
import org.mockito.scalatest.ResetMocksAfterEachTest

class RuleControllerSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {
  import play.api.test.Helpers._
  import org.mockito.IdiomaticMockito._

  val ruleServiceMock = mock[RuleService]
  val subject = new RuleController(ruleServiceMock, Helpers.stubControllerComponents())

  "saveRules" must {
    "return successIds correctly" in {
      // arrange
      val rules = List(getSampleRule())
      val json = Json.toJson(rules)
      val request = FakeRequest(POST, "api/rules", Headers.create(), json)

      // mock result data
      ruleServiceMock.saveRules(*) returns ZIO.succeed(SaveRulesResponseDTO(List("id1"), Map.empty))

      // act
      val result = subject.saveRules().apply(request)

      // assure
      status(result) mustBe OK
      val jsValue = Json.parse(contentAsString(result))
      jsValue.validate[SaveRulesResponseDTO] match {
        case JsSuccess(data, _) =>
          data.successIds.head mustBe "id1"
        case _: JsError =>
          fail("Invalid json in result ")
      }
    }

    "return BadRequest for incorrect input" in {

      // arrange
      val rules = "rules-in-incorrect-format"
      val json = Json.toJson(rules)
      val request = FakeRequest(POST, "api/rules", Headers.create(), json)

      // act
      val result = subject.saveRules().apply(request)

      // assure
      status(result) mustBe BAD_REQUEST
      ruleServiceMock wasNever called
    }

    "return InternalServerError for processing error" in {
      // arrange
      val rules = List(getSampleRule())
      val json = Json.toJson(rules)
      val request = FakeRequest(POST, "api/rules", Headers.create(), json)

      // mock result data
      ruleServiceMock.saveRules(*) returns ZIO.fail(new Exception("Unable to access database"))

      // act
      val result = subject.saveRules().apply(request)

      // assure
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "Unable to access database"
    }
  }

  "greet" must {
    "always return success" in {
      // arrange
      val request = FakeRequest(GET, "api/greet")

      // act
      val result = subject.greet().apply(request)

      // assert
      status(result) mustBe OK
    }
  }

  private def getSampleRule() = Rule("id1", "A > B", "id1")
}
