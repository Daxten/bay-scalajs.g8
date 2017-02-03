
package models.slick

import bay.driver.CustomizedPgDriver
import java.time._
import io.circe._
import shared.models.slick.default._

object Default extends {
  val profile = bay.driver.CustomizedPgDriver
} with Default

trait Default {

  val profile: bay.driver.CustomizedPgDriver
  import profile.api._

  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  
               
  /** GetResult implicit for fetching UserGroup objects using plain SQL queries */
  implicit def GetResultUserGroup(implicit e0: GR[String]): GR[UserGroup] = GR{
    prs => import prs._
    UserGroup(<<[String])
  }
  /** Table description of table UserGroup. Objects of this class serve as prototypes for rows in queries. */
  class UserGroupTable(_tableTag: Tag) extends profile.api.Table[UserGroup](_tableTag, "UserGroup") {
    def * = name <> (UserGroup, UserGroup.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = Rep.Some(name).shaped.<>(r => r.map(_=> UserGroup(r.get)), (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column name SqlType(varchar), PrimaryKey */
    val name: Rep[String] = column[String]("name", O.PrimaryKey)
  }
  /** Collection-like TableQuery object for table userGroups */
  lazy val userGroups = new TableQuery(tag => new UserGroupTable(tag))

  
               
  /** GetResult implicit for fetching User objects using plain SQL queries */
  implicit def GetResultUser(implicit e0: GR[String], e1: GR[OffsetDateTime], e2: GR[Option[OffsetDateTime]], e3: GR[Option[String]], e4: GR[Option[Int]]): GR[User] = GR{
    prs => import prs._
    val r = (<<?[Int], <<[String], <<[String], <<[OffsetDateTime], <<?[OffsetDateTime], <<?[OffsetDateTime], <<?[String])
    import r._
    User.tupled((_2, _3, _4, _5, _6, _7, _1)) // putting AutoInc last
  }
  /** Table description of table User. Objects of this class serve as prototypes for rows in queries. */
  class UserTable(_tableTag: Tag) extends profile.api.Table[User](_tableTag, "User") {
    def * = (email, password, created, lastLogin, lastAction, resetPasswordToken, Rep.Some(id)) <> (User.tupled, User.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(email), Rep.Some(password), Rep.Some(created), lastLogin, lastAction, resetPasswordToken, Rep.Some(id)).shaped.<>({r=>import r._; _1.map(_=> User.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column email SqlType(varchar) */
    val email: Rep[String] = column[String]("email")
    /** Database column password SqlType(varchar) */
    val password: Rep[String] = column[String]("password")
    /** Database column created SqlType(timestamptz) */
    val created: Rep[OffsetDateTime] = column[OffsetDateTime]("created")
    /** Database column lastLogin SqlType(timestamptz), Default(None) */
    val lastLogin: Rep[Option[OffsetDateTime]] = column[Option[OffsetDateTime]]("lastLogin", O.Default(None))
    /** Database column lastAction SqlType(timestamptz), Default(None) */
    val lastAction: Rep[Option[OffsetDateTime]] = column[Option[OffsetDateTime]]("lastAction", O.Default(None))
    /** Database column resetPasswordToken SqlType(varchar), Default(None) */
    val resetPasswordToken: Rep[Option[String]] = column[Option[String]]("resetPasswordToken", O.Default(None))
    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)

    /** Uniqueness Index over (email) (database name User_email_key) */
    val index1 = index("User_email_key", email, unique=true)
  }
  /** Collection-like TableQuery object for table users */
  lazy val users = new TableQuery(tag => new UserTable(tag))

  
               
  /** GetResult implicit for fetching UserToUserGroup objects using plain SQL queries */
  implicit def GetResultUserToUserGroup(implicit e0: GR[Int], e1: GR[String]): GR[UserToUserGroup] = GR{
    prs => import prs._
    val r = (<<[Int], <<[String])
    import r._
    UserToUserGroup.tupled((_1, _2)) // putting AutoInc last
  }
  /** Table description of table UserToUserGroup. Objects of this class serve as prototypes for rows in queries. */
  class UserToUserGroupTable(_tableTag: Tag) extends profile.api.Table[UserToUserGroup](_tableTag, "UserToUserGroup") {
    def * = (userId, groupName) <> (UserToUserGroup.tupled, UserToUserGroup.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(groupName)).shaped.<>({r=>import r._; _1.map(_=> UserToUserGroup.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column userId SqlType(int4) */
    val userId: Rep[Int] = column[Int]("userId")
    /** Database column groupName SqlType(varchar) */
    val groupName: Rep[String] = column[String]("groupName")

    /** Foreign key referencing users (database name UserToUserGroup_userId_fkey) */
    lazy val userTableFk = foreignKey("UserToUserGroup_userId_fkey", userId, users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing userGroups (database name UserToUserGroup_groupName_fkey) */
    lazy val userGroupTableFk = foreignKey("UserToUserGroup_groupName_fkey", groupName, userGroups)(r => r.name, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table userToUserGroups */
  lazy val userToUserGroups = new TableQuery(tag => new UserToUserGroupTable(tag))

}
     