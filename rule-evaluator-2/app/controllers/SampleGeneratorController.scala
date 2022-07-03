package io.github.iamsurajgharat
package ruleevaluator
package controllers

import javax.inject._
import play.api.mvc._
import play.api.mvc.ControllerComponents
import io.github.iamsurajgharat.ruleevaluator._
import io.github.iamsurajgharat.expressiontree.expressiontree.RecordImpl
import io.github.iamsurajgharat.expressiontree.expressiontree.RText
import io.github.iamsurajgharat.expressiontree.expressiontree.Record
import play.api.libs.json.Json

@Singleton
class SampleGeneratorController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {
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
