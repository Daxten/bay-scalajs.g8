package shared.models.swagger.smartresponse.v1
import java.time._
case class ApiTicket(timeslot: Option[OffsetDateTime], telephone: Option[String], closed: Boolean, key: String, media: List[String])
