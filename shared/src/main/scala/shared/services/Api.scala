package services

import org.threeten.bp.OffsetDateTime
import shared.models.ApiModel.ApiResult

trait Api {
  def ping(): ApiResult[String]
  def now(): ApiResult[OffsetDateTime]

  def createLoremIpsum(): ApiResult[List[String]]
}
