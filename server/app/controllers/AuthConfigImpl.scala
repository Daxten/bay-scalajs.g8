package controllers

import jp.t2v.lab.play2.auth._
import models.BaseModel.BaseUser
import play.api.mvc.{Controller, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._

trait AuthConfigImpl
  extends AuthConfig
    with Controller {

  /*
    Test Users
   */
  val users = Seq(
    BaseUser(1, "I'm a User")
  )

  type Id = Int
  type User = BaseUser
  type Authority = User => Future[Boolean]
  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600 * 24

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = Future.successful(users.find(_.id == id))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = Future.successful(Redirect(routes.Application.index("")))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = Future.successful(Redirect(routes.Application.index("")))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = Future.successful(Unauthorized)

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = Future.successful(Unauthorized)

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = authority(user)

  // Define reusable authorities

  def isLoggedIn(user: User) = Future.successful(true)

}