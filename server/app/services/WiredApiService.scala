package services

import java.time.OffsetDateTime
import shared.models.WiredApiModel.ApiResult
import shared.utils.Implicits._
import shared.utils.LoremIpsum
import shared.models.slick.default._
import shared.services.WiredApi
import cats.syntax.either._

class WiredApiService(user: Option[User]) extends WiredApi {

  override def ping(): ApiResult[String] = "pong".asRight.asFuture

  override def now(): ApiResult[OffsetDateTime] = OffsetDateTime.now.asRight.asFuture

  override def createLoremIpsum(): ApiResult[List[String]] = {
    Thread.sleep(2000)
    LoremIpsum.paragraphs(15).asRight.asFuture
  }
}
