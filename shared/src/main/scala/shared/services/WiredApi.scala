package shared.services

import java.time.OffsetDateTime

import shared.models.WiredApiModel.ApiResult

trait WiredApi {
  def ping(): ApiResult[String]
  def now(): ApiResult[OffsetDateTime]

  def createLoremIpsum(): ApiResult[List[String]]
}
