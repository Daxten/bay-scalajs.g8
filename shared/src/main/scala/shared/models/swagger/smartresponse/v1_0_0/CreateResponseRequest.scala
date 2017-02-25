package shared.models.swagger.smartresponse.v1_0_0
import java.time._
case class CreateResponseRequest(timeslot: Option[Double], telephone: String, problemcategory: String)
