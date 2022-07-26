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
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.SaveConfigAndMetadataResponseDTO
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.RuleMetadata
import _root_.io.github.iamsurajgharat.expressiontree.expressiontree.DataType
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.SaveConfigAndMetadataRequestDTO
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.GetRulesResponseDTO
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.EvaluateRulesRequestDTO
import _root_.io.github.iamsurajgharat.expressiontree.expressiontree.RecordImpl
import _root_.io.github.iamsurajgharat.expressiontree.expressiontree.RText
import _root_.io.github.iamsurajgharat.ruleevaluator.models.web.EvaluateRulesResponseDTO
import _root_.io.github.iamsurajgharat.ruleevaluator.models.domain.EvalResult

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
      contentAsString(result) must include("Hello")
    }
  }

  "saveConfigAndMetadata" must {
    "return success result correctly" in {
      // arrange
      val ruleMetadata = RuleMetadata(Map("field1" -> DataType.Number))
      val json = Json.toJson(SaveConfigAndMetadataRequestDTO(ruleMetadata))
      val request = FakeRequest(POST, "api/rules/metadata", Headers.create(), json)

      // mock result data
      ruleServiceMock.saveConfigAndMetadata(*) returns ZIO.succeed(
        SaveConfigAndMetadataResponseDTO(ruleMetadata)
      )

      // act
      val result = subject.saveConfigAndMetadata().apply(request)

      // assure
      status(result) mustBe OK
      val jsValue = Json.parse(contentAsString(result))
      jsValue.validate[SaveConfigAndMetadataResponseDTO] match {
        case JsSuccess(metadata, _) =>
          metadata.metadata.dataTypes("field1") mustBe DataType.Number
        case _: JsError =>
          fail("Invalid json in result ")
      }
    }

    "return BadRequest for incorrect input" in {

      // arrange
      val json = Json.toJson("Invalid-json")
      val request = FakeRequest(POST, "api/rules/metadata", Headers.create(), json)

      // act
      val result = subject.saveConfigAndMetadata().apply(request)

      // assure
      status(result) mustBe BAD_REQUEST
      ruleServiceMock wasNever called
    }

    "return InternalServerError for processing error" in {
      // arrange
      val ruleMetadata = RuleMetadata(Map("field1" -> DataType.Number))
      val json = Json.toJson(SaveConfigAndMetadataRequestDTO(ruleMetadata))
      val request = FakeRequest(POST, "api/rules/metadata", Headers.create(), json)

      // mock result data
      ruleServiceMock.saveConfigAndMetadata(*) returns ZIO.fail(new Exception("Unable to access database"))

      // act
      val result = subject.saveConfigAndMetadata().apply(request)

      // assure
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "Unable to access database"
    }
  }

  "getRules" must {
    "return success result correctly" in {
      // arrange
      val ids = Set("id1", "id2")
      val json = Json.toJson(ids)
      val request = FakeRequest(POST, "api/rules", Headers.create(), json)

      // mock result data
      ruleServiceMock.getRules(*) returns ZIO.succeed(
        GetRulesResponseDTO(List(getSampleRule()))
      )

      // act
      val result = subject.getRules().apply(request)

      // assure
      status(result) mustBe OK
      val jsValue = Json.parse(contentAsString(result))
      jsValue.validate[GetRulesResponseDTO] match {
        case JsSuccess(res, _) =>
          res.data.size mustBe(1)
        case _: JsError =>
          fail("Invalid json in result ")
      }
    }

    "return BadRequest for incorrect input" in {

      // arrange
      val json = Json.toJson("Invalid-json")
      val request = FakeRequest(POST, "api/rules", Headers.create(), json)

      // act
      val result = subject.getRules().apply(request)

      // assure
      status(result) mustBe BAD_REQUEST
      ruleServiceMock wasNever called
    }

    "return InternalServerError for processing error" in {
      // arrange
      val ids = Set("id1", "id2")
      val json = Json.toJson(ids)
      val request = FakeRequest(POST, "api/rules", Headers.create(), json)

      // mock result data
      ruleServiceMock.getRules(*) returns ZIO.fail(new Exception("Unable to access database"))

      // act
      val result = subject.getRules().apply(request)

      // assure
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "Unable to access database"
    }
  }

  "evalRules" must {
    "return success result correctly" in {
      // arrange
      val requestData = EvaluateRulesRequestDTO(None, List(new RecordImpl(Map("field1" -> RText("Value1")))))
      val json = Json.toJson(requestData)
      val request = FakeRequest(POST, "api/rules/eval", Headers.create(), json)

      // mock result data
      ruleServiceMock.evalRules(*) returns ZIO.succeed(
        EvaluateRulesResponseDTO(Map("id1" -> List(EvalResult(true, "r1", ""))))
      )

      // act
      val result = subject.evalRules().apply(request)

      // assure
      status(result) mustBe OK
      val jsValue = Json.parse(contentAsString(result))
      jsValue.validate[EvaluateRulesResponseDTO] match {
        case JsSuccess(res, _) =>
          res.data("id1").head.result mustBe("r1")
        case _: JsError =>
          fail("Invalid json in result ")
      }
    }

    "return BadRequest for incorrect input" in {

      // arrange
      val json = Json.toJson("Invalid-json")
      val request = FakeRequest(POST, "api/rules/eval", Headers.create(), json)

      // act
      val result = subject.evalRules().apply(request)

      // assure
      status(result) mustBe BAD_REQUEST
      ruleServiceMock wasNever called
    }

    "return InternalServerError for processing error" in {
      // arrange
      val requestData = EvaluateRulesRequestDTO(None, List(new RecordImpl(Map("field1" -> RText("Value1")))))
      val json = Json.toJson(requestData)
      val request = FakeRequest(POST, "api/rules/eval", Headers.create(), json)

      // mock result data
      ruleServiceMock.evalRules(*) returns ZIO.fail(new Exception("Unable to access database"))

      // act
      val result = subject.evalRules().apply(request)

      // assure
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "Unable to access database"
    }
  }

  private def getSampleRule() = Rule("id1", "A > B", "id1")
}
