package services

import java.time.OffsetDateTime

import scala.concurrent.Future

trait Api {
  def ping(): Future[String]
  def now(): Future[OffsetDateTime]
}
