package io.github.iamsurajgharat
package ruleevaluator
package helpers

import akka.serialization.Serializer
import io.github.iamsurajgharat.expressiontree.expressiontree.RecordImpl
import io.github.iamsurajgharat.expressiontree.expressiontree.Record
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError

class RecordSerializer extends Serializer {

    println("########## Initializing RecordSerializer....")

   import io.github.iamsurajgharat.ruleevaluator.models.web.Rule.recordReads;
   import io.github.iamsurajgharat.ruleevaluator.models.web.Rule.recordWrites;
  // If you need logging here, introduce a constructor that takes an ExtendedActorSystem.
  // class MyOwnSerializer(actorSystem: ExtendedActorSystem) extends Serializer
  // Get a logger using:
  // private val logger = Logging(actorSystem, this)

  // This is whether "fromBinary" requires a "clazz" or not
  def includeManifest: Boolean = true

  // Pick a unique identifier for your Serializer,
  // you've got a couple of billions to choose from,
  // 0 - 40 is reserved by Akka itself
  def identifier = 7654321

  // "toBinary" serializes the given object to an Array of Bytes
  def toBinary(obj: AnyRef): Array[Byte] = {
    val record = obj.asInstanceOf[Record]
    Json.toBytes(Json.toJson(record))
  }

  // "fromBinary" deserializes the given array,
  // using the type hint (if any, see "includeManifest" above)
  def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    val json = Json.parse(bytes)
    Json.fromJson[Record](json).fold(
        x => ???,
        y => y
    )
  }
}

