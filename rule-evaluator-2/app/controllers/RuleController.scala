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

@Singleton
class RuleController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {
  import helpers.ZIOHelper._
  import models.web._
  def saveRules(): Action[JsValue] = zioActionWithBody(request => {
    def validateError(
        errors: scala.collection.Seq[
          (JsPath, scala.collection.Seq[JsonValidationError])
        ]
    ): UIO[Result] = {
      ???
      // val validationError = ZIO
      //   .attempt(Json.obj("errors" -> JsError.toJson(errors)))
      //   .orElse(ZIO.succeed(Json.obj("errors" -> "Error in parsing input")))
      // validationError.map(BadRequest(_))
    }

    def validateSuccess(rates: List[Rule]): UIO[Result] = {
      ???
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
