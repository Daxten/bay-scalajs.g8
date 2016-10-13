package services

import org.threeten.bp.OffsetDateTime

import shared.models.SharedDefault.BaseUser

import scala.concurrent.Future

class ApiService(user: Option[BaseUser]) extends Api {

  def ping(): Future[String] = "pong"

  override def now(): Future[OffsetDateTime] = OffsetDateTime.now
}
