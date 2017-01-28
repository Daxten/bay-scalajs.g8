package shared.services

import java.time.OffsetDateTime
import shared.models.AutoApiModel.ApiResult

trait AutoApi {
  def ping(): ApiResult[String]
  def now(): ApiResult[OffsetDateTime]

  def createLoremIpsum(): ApiResult[List[String]]
}
