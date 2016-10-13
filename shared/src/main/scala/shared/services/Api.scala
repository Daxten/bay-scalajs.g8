package services

import org.threeten.bp.OffsetDateTime

import scala.concurrent.Future

trait Api {
  def ping(): Future[String]
  def now(): Future[OffsetDateTime]
}
