package shared.models.swagger.smartresponse.v1
import java.time._
case class ApiTicketInfo(timeslot: Option[OffsetDateTime], telephone: Option[String], closed: Boolean)
