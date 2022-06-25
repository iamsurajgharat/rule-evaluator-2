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
import play.api.libs.json.Writes
import io.github.iamsurajgharat.expressiontree.expressiontree.RecordImpl
import io.github.iamsurajgharat.expressiontree.expressiontree.RText
import play.api.libs.json.Format
import io.github.iamsurajgharat.expressiontree.expressiontree.Record

@Singleton
class SampleGeneratorController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {
  import helpers.ZIOHelper._
  import models.web._

  def getSampleXYPayload(start:Int, count:Int) = Action { _ =>
    import Rule._
    val rules = (start to (start + count)).map(x => 
      (new RecordImpl(
        Map(
          "id" -> RText("id-"+x), 
          "expression" -> RText("B > C"), 
          "result" -> RText("id-"+x)
        )
      )).asInstanceOf[Record]).toList

    Ok(Json.toJson(rules))
  }
}
