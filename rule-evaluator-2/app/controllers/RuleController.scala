package io.github.iamsurajgharat
package ruleevaluator
package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.ControllerComponents
import play.api.libs.json.JsValue
import play.api.libs.json.JsPath
import play.api.libs.json.Json
import play.api.libs.json.JsError
import play.api.libs.json.JsonValidationError
import zio.UIO
import zio.ZIO
import io.github.iamsurajgharat.ruleevaluator._
import io.github.iamsurajgharat.ruleevaluator.services.RuleService
import scala.util.Try
import zio.Task

@Singleton
class RuleController @Inject() (private val ruleService:RuleService, val controllerComponents: ControllerComponents)
    extends BaseController {
  import helpers.ZIOHelper._
  import models.web._

  def greet() = Action { _ =>
    Ok("Hello from Rule Evaluator!")
  }

  def saveRules(): Action[JsValue] = zioActionWithBody(request => {
    request.body
      .validate[List[Rule]]
      .fold(validateError, req => validateSuccess(req, ruleService.saveRules))
  })

  def saveConfigAndMetadata(): Action[JsValue] = zioActionWithBody(request => {
    request.body
      .validate[SaveConfigAndMetadataRequestDTO]
      .fold(validateError, req => validateSuccess(req, ruleService.saveConfigAndMetadata))
  })

  def getRules() : Action[JsValue] = zioActionWithBody(request => {

    request.body
      .validate[Set[String]]
      .fold(validateError, req => validateSuccess(req, ruleService.getRules))

  })

  def evalRules() : Action[JsValue] = zioActionWithBody(request => {
    request.body
      .validate[EvaluateRulesRequestDTO]
      .fold(validateError, req => validateSuccess(req, ruleService.evalRules))
  })

  def deleteRules() = ???
  def evalute() = ???

  def zioActionWithBody(actionFun: Request[JsValue] => UIO[Result]): Action[JsValue] = {
    Action(parse.json) { request =>
      ((interpret[Result] _) compose actionFun)(request)
    }
  }

  private def validateError(
        errors: scala.collection.Seq[
          (JsPath, scala.collection.Seq[JsonValidationError])
        ]
  ): UIO[Result] = {
      ZIO
        .fromTry(Try(Json.obj("errors" -> JsError.toJson(errors))))
        .orElse(ZIO.succeed(Json.obj("errors" -> "Error in parsing input")))
        .map(BadRequest(_))
    }

  private def validateSuccess[T1,T2 <: BaseDTO](requestData: T1, handler:T1 => Task[T2]): UIO[Result] = {
    handler(requestData).fold(
      errors => {
        println(Console.RED + "Failed!!!" + Console.RESET)
        InternalServerError(errors.getMessage())
      },
      result => {
        println(Console.GREEN + "Passed!!!" + Console.RESET)
        Ok(toJson(result))
      }
    )
  }

  private def toJson[T <: BaseDTO](data : T) : JsValue = data match {
        case req : SaveRulesResponseDTO => Json.toJson(req)
        case res : GetRulesResponseDTO => Json.toJson(res)
        case res : SaveConfigAndMetadataResponseDTO => Json.toJson(res)
        case res : EvaluateRulesResponseDTO => Json.toJson(res)
  }
}
