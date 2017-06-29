package services.dao

import java.time._

import bay.driver.CustomizedPgDriver
import com.github.t3hnar.bcrypt._
import models.forms.LoginForm
import models.slick.Default._
import play.api.Logger
import play.api.db.slick.HasDatabaseConfig
import shared.models.slick.default._
import shared.utils.Implicits
import slick.basic

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class UserDao(protected val dbConfig: basic.DatabaseConfig[CustomizedPgDriver])
    extends HasDatabaseConfig[CustomizedPgDriver]
    with Implicits {

  import profile.api._

  def findUserByEmail(email: String): Future[Option[User]] =
    db.run(users.filter(_.email === email.toLowerCase).result.headOption)

  def findUserById(id: Int): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  def resolveUser(id: Int)(implicit ec: ExecutionContext): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption).map { e =>
      if (e.isDefined) { // don't wait for this update
        db.run(users.filter(_.id === id).map(_.lastAction).update(OffsetDateTime.now.asOption))
      }

      e
    }

  def isInGroup(id: Option[Int], groupName: String): Future[Boolean] =
    db.run(
      userToUserGroups
        .filter(e => e.userId === id && e.groupName === groupName.toLowerCase)
        .exists
        .result)

  def maybeLogin(loginForm: LoginForm)(implicit ec: ExecutionContext): Future[Option[Int]] =
    db.run(users.filter(_.email === loginForm.email).result.headOption).flatMap {
      case Some(user) if loginForm.password.isBcrypted(user.password) =>
        db.run(users.filter(_.id === user.id).map(_.lastLogin).update(OffsetDateTime.now.asOption))
          .map { n =>
            if (n > 0) {
              Logger.info(s"${user.email} logged in")
              user.id
            } else None
          }
      case _ =>
        None.asFuture
    }
}
