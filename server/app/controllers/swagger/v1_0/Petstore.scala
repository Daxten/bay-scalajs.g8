package controllers.swagger.v1_0

import play.api.mvc._
import com.google.inject.Inject
import play.api.routing._
import play.api.routing.sird._
import play.api.libs.circe._

import scala.concurrent.ExecutionContext
import controllers.{AuthConfigImpl, ExtendedController}
import jp.t2v.lab.play2.stackc.RequestWithAttributes
import jp.t2v.lab.play2.auth.OptionalAuthElement
import services.dao.UserDao
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.Files
import shared.models.swagger.petstore.v1_0._

class Petstore @Inject()(val userDao: UserDao)(implicit val ec: ExecutionContext) extends PetstoreTrait {

  def findPets(tags: Option[String], limit: Option[String])(implicit request: RequestWithAttributes[AnyContent]): HttpResult[Result] =
    NotImplemented.pureResult
  def addPet()(implicit request: RequestWithAttributes[MultipartFormData[Files.TemporaryFile]]): HttpResult[Result] = NotImplemented.pureResult
  def findPetById(id: String)(implicit request: RequestWithAttributes[AnyContent]): HttpResult[Result]              = NotImplemented.pureResult
  def deletePet(id: String)(implicit request: RequestWithAttributes[AnyContent]): HttpResult[Result]                = NotImplemented.pureResult

}

trait PetstoreTrait extends ExtendedController with SimpleRouter with OptionalAuthElement with AuthConfigImpl with Circe {
  def routes: Router.Routes = {

    case GET(p"/pets" ? q_o"tags=$tags" ? q_o"limit=$limit") =>
      AsyncStack { implicit request =>
        constructResult(findPets(tags, limit))
      }

    case POST(p"/pets") =>
      AsyncStack(parse.multipartFormData) { implicit request =>
        constructResult(addPet())
      }

    case GET(p"/pets/${id}") =>
      AsyncStack { implicit request =>
        constructResult(findPetById(id))
      }

    case DELETE(p"/pets/${id}") =>
      AsyncStack { implicit request =>
        constructResult(deletePet(id))
      }

  }

  def findPets(tags: Option[String], limit: Option[String])(implicit request: RequestWithAttributes[AnyContent]): HttpResult[Result]
  def addPet()(implicit request: RequestWithAttributes[MultipartFormData[Files.TemporaryFile]]): HttpResult[Result]
  def findPetById(id: String)(implicit request: RequestWithAttributes[AnyContent]): HttpResult[Result]
  def deletePet(id: String)(implicit request: RequestWithAttributes[AnyContent]): HttpResult[Result]
}
