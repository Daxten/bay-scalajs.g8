package services

import java.time.OffsetDateTime

import play.api.libs.Files
import play.api.mvc.MultipartFormData
import shared.models.WiredApiModel.ApiResult
import shared.models.slick.default._
import shared.services.WiredApi
import shared.utils.Implicits
import shared.utils.LoremIpsum

class WiredApiService(user: User,
                      services: Services,
                      files: Seq[MultipartFormData.FilePart[Files.TemporaryFile]])
    extends WiredApi
    with Implicits {

  override def ping(): ApiResult[String] = "pong".asResult

  override def now(): ApiResult[OffsetDateTime] = OffsetDateTime.now.asResult

  override def createLoremIpsum(): ApiResult[List[String]] = {
    Thread.sleep(2000)
    LoremIpsum.paragraphs(15).asResult
  }
}
