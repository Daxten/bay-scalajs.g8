package shared.models.swagger.smartresponse.v1
import java.time._
case class UserData(telephone: List[TelephoneNumber], firstname: Option[String], lastname: Option[String], email: String)
