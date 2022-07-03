package io.github.iamsurajgharat
package ruleevaluator
package models.web

import play.api.libs.json._
import io.github.iamsurajgharat.expressiontree.expressiontree.DataType
import io.github.iamsurajgharat.ruleevaluator.models.domain.EvalResult
import io.github.iamsurajgharat.expressiontree.expressiontree.Record
import io.github.iamsurajgharat.expressiontree.expressiontree.RBool
import io.github.iamsurajgharat.expressiontree.expressiontree.RDate
import io.github.iamsurajgharat.expressiontree.expressiontree.RDateTime
import io.github.iamsurajgharat.expressiontree.expressiontree.RNumber
import io.github.iamsurajgharat.expressiontree.expressiontree.RText
import io.github.iamsurajgharat.expressiontree.expressiontree.RecordImpl
import io.github.iamsurajgharat.expressiontree.expressiontree.RValue

case class Rule(id:String, expression:String, result:String)

object Rule {
    implicit val format = Json.format[Rule]
    implicit val recordWrites = new Writes[Record] {
        def writes(record: Record) = {
            val newMap = new scala.collection.mutable.HashMap[String,JsValue]
            for(key <- record.getKeys()) {
                record.getValue(key) match {
                    case None => 
                    case Some(value) => 
                        value match {
                            case RBool(data) => newMap += ((key, JsBoolean(data)))
                            case RDate(data) => newMap += ((key, JsString(data.toString())))
                            case RDateTime(data) => newMap += ((key, JsString(data.toString())))
                            case RNumber(data) => newMap += ((key, JsNumber(data)))
                            case RText(data) => newMap += ((key, JsString(data)))
                        }
                }
            }
            JsObject(newMap)
        }
    }

    implicit val recordReads = new Reads[Record] {
        def reads(json: JsValue): JsResult[Record] = {
            def jsValueToRValue(jsValue:JsValue) : RValue = jsValue match {
                case JsBoolean(data) => RBool(data)
                case JsString(value) => RText(value)
                case JsNumber(value) => RNumber(value.toFloat)
                case _ => ???
            }

            json match {
                case JsObject(underlying) => JsSuccess(new RecordImpl(underlying.map(x => (x._1, jsValueToRValue(x._2))).toMap))
                case _ => JsError("Record must be JsObject")
            }
        }
    }
}

case class RuleMetadata(dataTypes:Map[String,DataType.DataType]) {
    def merge(that:RuleMetadata) = copy(dataTypes = dataTypes ++ that.dataTypes)
    def ++(that:RuleMetadata) = merge(that)
}

object RuleMetadata {
    val empty = new RuleMetadata(Map.empty)
    implicit val ruleMetadataWrites = new Writes[RuleMetadata] {
        def dataTypeToString(dt:DataType.DataType):String = {
            dt.toString()
        }

        def writes(o: RuleMetadata): JsValue = {
            val newMap = o.dataTypes.map(x => x._1 -> JsString(dataTypeToString(x._2)))
            JsObject(Map.empty[String,JsValue]) + ("dataTypes", JsObject(newMap))
        }
    }

    implicit val ruleMetadataReads = new Reads[RuleMetadata] {
        def stringToDataType(str:String):DataType.DataType = {
            DataType.withName(str)
        }
        def reads(json: JsValue): JsResult[RuleMetadata] = {
            json match {
                case JsObject(underlying) => 
                    val datTypes = underlying("dataTypes")
                    datTypes match {
                        case JsObject(underlying2) => 
                            val res = underlying2
                                .map(x => x._1 -> x._2.asInstanceOf[JsString])
                                .map(x => x._1 -> stringToDataType(x._2.value)).toMap
                            JsSuccess(RuleMetadata(res))
                        case _ =>
                            JsError("""Missing required property "dataTypes"""")
                    }
                case _ =>
                    JsError("RuleMetadata has object structure")   
            }
        }
    }
}

sealed trait BaseDTO{}

case class SaveRulesResponseDTO(successIds:List[String], errors:Map[String,String]) extends BaseDTO

object SaveRulesResponseDTO {
    implicit val format = Json.format[SaveRulesResponseDTO]
}

case class GetRulesResponseDTO(data:List[Rule]) extends BaseDTO

object GetRulesResponseDTO {
    implicit val format = Json.format[GetRulesResponseDTO]
}

case class EvalConfig(flag1:Boolean)

object EvalConfig {
    implicit val format = Json.format[EvalConfig]
}

case class EvaluateRulesRequestDTO(context:Option[EvalConfig], records:List[Record]) extends BaseDTO

object EvaluateRulesRequestDTO {
    import Rule._
    implicit val format = Json.format[EvaluateRulesRequestDTO]
}

case class EvaluateRulesResponseDTO(data:Map[String, List[EvalResult]]) extends BaseDTO

object EvaluateRulesResponseDTO {
    implicit val format = Json.format[EvaluateRulesResponseDTO]
}

case class SaveConfigAndMetadataRequestDTO(metadata:RuleMetadata) extends BaseDTO
case class SaveConfigAndMetadataResponseDTO(metadata:RuleMetadata) extends BaseDTO

object SaveConfigAndMetadataRequestDTO{
    implicit val format = Json.format[SaveConfigAndMetadataRequestDTO]
}

object SaveConfigAndMetadataResponseDTO {
    implicit val format = Json.format[SaveConfigAndMetadataResponseDTO]
}