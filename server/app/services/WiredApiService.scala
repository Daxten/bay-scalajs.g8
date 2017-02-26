package services

import java.time.OffsetDateTime
import shared.models.WiredApiModel.ApiResult
import shared.utils.Implicits._
import shared.utils.LoremIpsum
import shared.models.slick.default._
import shared.services.WiredApi
import scalaz.Scalaz._
import scalaz._

class WiredApiService(user: Option[User]) extends WiredApi {

  override def ping(): ApiResult[String] = "pong".right.asFuture

  override def now(): ApiResult[OffsetDateTime] = OffsetDateTime.now.right.asFuture

  override def createLoremIpsum(): ApiResult[List[String]] = {
    Thread.sleep(2000)
    LoremIpsum.paragraphs(15).right.asFuture
  }
}
