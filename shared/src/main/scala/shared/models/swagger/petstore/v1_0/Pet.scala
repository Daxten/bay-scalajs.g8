package shared.models.swagger.petstore.v1_0
import java.time._
case class Pet(id: Long, name: String, created: OffsetDateTime, tag: Option[String])
