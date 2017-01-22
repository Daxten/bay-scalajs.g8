
package shared.models.auto_generated

trait SharedDefault {
  case class UserGroup(name: String)
  case class User(email: String, password: String, created: org.threeten.bp.OffsetDateTime, lastLogin: Option[org.threeten.bp.OffsetDateTime] = None, lastAction: Option[org.threeten.bp.OffsetDateTime] = None, resetPasswordToken: Option[String] = None, id: Option[Int] = None)
  case class UserToUserGroup(userId: Int, groupName: String)
}
     