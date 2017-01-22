package services

import org.threeten.bp.OffsetDateTime
import shared.models.ApiModel.ApiResult
import shared.models.SharedDefault._
import shared.utils.Implicits._
import shared.utils.LoremIpsum
import scalaz.Scalaz._
import scalaz._

class ApiService(user: Option[User]) extends Api {

  override def ping(): ApiResult[String] = "pong".right.asFuture

  override def now(): ApiResult[OffsetDateTime] = OffsetDateTime.now.right.asFuture

  override def createLoremIpsum(): ApiResult[List[String]] = {
    Thread.sleep(2000)
    LoremIpsum.paragraphs(15).right.asFuture
  }
}
