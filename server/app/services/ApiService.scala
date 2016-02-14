package services

import models.BaseModel.BaseUser

import scala.concurrent.Future

class ApiService(user: Option[BaseUser]) extends Api {

  def ping() = Future.successful("pong")

}