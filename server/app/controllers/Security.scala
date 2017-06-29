package controllers

import cats.implicits._
import play.api.mvc.Results._
import play.api.mvc._
import services.dao.UserDao
import shared.models.slick.default.User

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

class Security(val parser: BodyParsers.Default, userDao: UserDao)(
    implicit val ec: ExecutionContext) {
  val sessionKey: String = "security.userid"

  private def findUserId(request: RequestHeader): Option[Int] =
    request.session.get(sessionKey).flatMap(e => Try(e.toInt).toOption)

  def onUnauthorized(request: RequestHeader): Result =
    Unauthorized.withSession(request.session - sessionKey)

  class UserIdRequest[A](val userId: Int, request: Request[A]) extends WrappedRequest[A](request)

  class OptUserIdRequest[A](val userId: Option[Int], request: Request[A])
      extends WrappedRequest[A](request)

  class UserRequest[A](val user: User, request: UserIdRequest[A])
      extends WrappedRequest[A](request)

  class OptUserRequest[A](val user: Option[User], request: OptUserIdRequest[A])
      extends WrappedRequest[A](request)

  class OptUserIdAction(val parser: BodyParsers.Default)(
      implicit val executionContext: ExecutionContext)
      extends ActionBuilder[OptUserIdRequest, AnyContent]
      with ActionTransformer[Request, OptUserIdRequest] {

    def transform[A](request: Request[A]): Future[OptUserIdRequest[A]] = Future.successful {
      new OptUserIdRequest(findUserId(request), request)
    }
  }

  val optUserIdAction = new OptUserIdAction(parser)

  private def UserIdActionImpl(implicit ec: ExecutionContext) =
    new ActionRefiner[OptUserIdRequest, UserIdRequest] {

      def refine[A](request: OptUserIdRequest[A]): Future[Either[Result, UserIdRequest[A]]] =
        Future.successful {
          request.userId match {
            case Some(userId) =>
              new UserIdRequest[A](userId, request).asRight
            case None =>
              onUnauthorized(request).asLeft
          }
        }

      override protected def executionContext: ExecutionContext = ec
    }

  private def UserActionImpl(implicit executionContext: ExecutionContext) =
    new ActionRefiner[UserIdRequest, UserRequest] {
      override protected def refine[A](
          request: UserIdRequest[A]): concurrent.Future[Either[Result, UserRequest[A]]] =
        userDao.findUserById(request.userId).map {
          case Some(user) =>
            new UserRequest(user, request).asRight
          case None =>
            onUnauthorized(request).asLeft
        }

      override protected def executionContext: ExecutionContext = ec
    }

  private def UserOptActionImpl(implicit executionContext: ExecutionContext) =
    new ActionRefiner[OptUserIdRequest, OptUserRequest] {
      override protected def refine[A](
          request: OptUserIdRequest[A]): concurrent.Future[Either[Result, OptUserRequest[A]]] =
        request.userId match {
          case Some(userId) =>
            userDao.findUserById(userId).map { userOpt =>
              new OptUserRequest(userOpt, request).asRight
            }
          case None =>
            Future.successful {
              new OptUserRequest(None, request).asRight
            }
        }

      override protected def executionContext: ExecutionContext = ec
    }

  def UserIdAction = optUserIdAction.andThen(UserIdActionImpl)

  def UserOptAction = optUserIdAction.andThen(UserOptActionImpl)

  def UserAction = UserIdAction.andThen(UserActionImpl)

}
