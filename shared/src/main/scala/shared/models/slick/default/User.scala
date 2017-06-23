package shared.models.slick.default
import shared.utils.Codecs._
import java.time._

case class User(email: String,
                password: String,
                created: OffsetDateTime,
                lastLogin: Option[OffsetDateTime] = None,
                lastAction: Option[OffsetDateTime] = None,
                resetPasswordToken: Option[String] = None,
                id: Option[Int] = None)
