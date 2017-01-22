package controllers

import jp.t2v.lab.play2.auth._
import play.api.mvc.{RequestHeader, Result}
import services.dao.UserDao
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._

trait AuthConfigImpl extends AuthConfig with ExtendedController {

  val userDao: UserDao

  type Id        = Int
  type User      = shared.models.SharedDefault.User
  type Authority = User => Future[Boolean]
  val idTag: ClassTag[Id]          = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    userDao.resolveUser(id)(ctx)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Redirect(routes.Application.index("")).asFuture

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Redirect(routes.Application.index("")).asFuture

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = Unauthorized.asFuture

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] =
    Unauthorized.asFuture

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = authority(user)

  // Define reusable authorities

  def isLoggedIn(user: User): Future[Boolean] = true.asFuture

  def isInGroup(groupName: String)(user: User): Future[Boolean] =
    userDao.isInGroup(user.id, groupName)
}
