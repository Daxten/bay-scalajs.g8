package app

import java.io.File

import io.circe.{Json, JsonObject}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.io.Source

/**
  * Created by alexe on 26.11.2016.
  */
object ApiGen extends App {
  import io.circe.parser._

  val rawJson = Source.fromFile(new File("C:\\Users\\alexe\\Desktop\\test.json")).mkString
  decode[ApiResult](rawJson) match {
    case Left(error) =>
      println("Error")
      println(error.getMessage)
    case Right(json) =>
      val paramList = createParamListFromJsonArray(json.stashes.flatMap(_.items))
      println(paramList)

      //paramList.filter(!_.contains("Option")).foreach(e => println("val " + e))
      paramList.filter(_.contains("Option")).foreach(e => println("val " + e))
  }

  def createTypeOverArray(list: List[JsonObject], field: String): String = {
    val filter = list.filter(_.contains(field))

    if (filter.forall(_(field).get.asNumber.isDefined))
      "Long"
    else if (filter.forall(_(field).get.asArray.isDefined))
      "List[Json]"
    else if (filter.forall(_(field).get.asObject.isDefined))
      "JsonObject"
    else if (filter.forall(_(field).get.asBoolean.isDefined))
      "Boolean"
    else
      "String"
  }

  def createParamListFromJsonArray(list: List[JsonObject]) = {
    val fields = list.flatMap(_.fields).distinct
    fields.map { field =>
      if (list.forall(_.fields.contains(field))) {
        s"$field: ${createTypeOverArray(list, field)}"
      } else {
        s"$field: Option[${createTypeOverArray(list, field)}]"
      }

    }
  }

}

sealed trait Item {
  val verified: Boolean
  val w: Long
  val h: Long
  val ilvl: Long
  val icon: String
  val league: String
  val id: String
  val sockets: List[Socket]
  val name: String
  val typeLine: String
  val identified: Boolean
  val corrupted: Boolean
  val lockedToCharacter: Boolean
  val frameType: Long
  val x: Long
  val y: Long
  val inventoryId: String
  val socketedItems: List[Json]
  val note: Option[String]
}
case class Socket(group: Int, attr: String)

case class BasicItem(
    verified: Boolean,
    w: Long,
    h: Long,
    ilvl: Long,
    icon: String,
    league: String,
    id: String,
    sockets: List[Socket],
    name: String,
    typeLine: String,
    identified: Boolean,
    corrupted: Boolean,
    lockedToCharacter: Boolean,
    frameType: Long,
    x: Long,
    y: Long,
    inventoryId: String,
    socketedItems: List[Json],
    note: Option[String]
) extends Item

case class Gem(
    verified: Boolean,
    w: Long,
    h: Long,
    ilvl: Long,
    icon: String,
    league: String,
    id: String,
    sockets: List[Socket],
    name: String,
    typeLine: String,
    identified: Boolean,
    corrupted: Boolean,
    lockedToCharacter: Boolean,
    frameType: Long,
    x: Long,
    y: Long,
    inventoryId: String,
    socketedItems: List[Json],
    note: Option[String],
    secDescrText: String
) extends Item

/*
val properties: Option[List[Json]]
val requirements: Option[List[Json]]
val explicitMods: Option[List[Json]]
val flavourText: Option[List[Json]]
val support: Option[Boolean]
val additionalProperties: Option[List[Json]]
val nextLevelRequirements: Option[List[Json]]
val secDescrText: Option[String]
val descrText: Option[String]
val implicitMods: Option[List[Json]]
val talismanTier: Option[Long]
val craftedMods: Option[List[Json]]
val artFilename: Option[String]
val duplicated: Option[Boolean]

 */

case class ApiResult(next_change_id: String, stashes: List[Stash])
case class Stash(accountName: String, lastCharacterName: String, id: String, stash: String, stashType: String, items: List[JsonObject], public: Boolean)
case class PoeItem(ilvl: Int)
