package shared.models.swagger.petstore.v1_0
import java.time._
case class NewPet(id: Option[Long], name: String, tag: Option[String])
