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

@Singleton
class RuleController @Inject() (private val ruleService:RuleService, val controllerComponents: ControllerComponents)
    extends BaseController {
  import helpers.ZIOHelper._
  import models.web._
  def saveRules(): Action[JsValue] = zioActionWithBody(request => {
    
    def validateError(
        errors: scala.collection.Seq[
          (JsPath, scala.collection.Seq[JsonValidationError])
        ]
    ): UIO[Result] = {
      val validationError = ZIO
        .fromTry(Try(Json.obj("errors" -> JsError.toJson(errors))))
        .orElse(ZIO.succeed(Json.obj("errors" -> "Error in parsing input")))
      validationError.map(BadRequest(_))
    }

    def validateSuccess(rules: List[Rule]): UIO[Result] = {
      ruleService.saveRules(rules).fold(
        errors => {
          println(Console.RED + "Failed!!!" + Console.RESET)
          InternalServerError("Error-ABCD")
        },
        result => {
          println(Console.RED + "Passed!!!" + Console.RESET)
          Ok(Json.toJson(result))
        }
      )
    }

    request.body
      .validate[List[Rule]]
      .fold(validateError, validateSuccess)
  })

  def getRules() = ???
  def deleteRules() = ???
  def evalute() = ???

  def zioActionWithBody(
      actionFun: Request[JsValue] => UIO[Result]
  ): Action[JsValue] = {
    Action(parse.json) { request =>
      ((interpret[Result] _) compose actionFun)(request)
    }
  }
}
