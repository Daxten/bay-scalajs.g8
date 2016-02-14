package services

import scala.concurrent.Future

trait Api {
  def ping(): Future[String]
}
