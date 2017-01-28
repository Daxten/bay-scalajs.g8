package controllers.swagger.v1_0

import com.google.inject.Inject
import play.api.mvc._
import play.api.routing._
import play.api.routing.sird._
import scala.concurrent.ExecutionContext
import controllers.{AuthConfigImpl, ExtendedController}
import jp.t2v.lab.play2.auth.OptionalAuthElement
import shared.models.swagger.petstore.v1_0._

trait Petstore extends ExtendedController with OptionalAuthElement with AuthConfigImpl {
  val swaggerRouter: Router = Router.from {

    case GET(p"/pets" ? q_o"tags=$tags" ? q_o"limit=$limit") =>
      AsyncStack { implicit request =>
        constructResult(findPets(tags, limit, loggedIn))
      }

    case POST(p"/pets") =>
      AsyncStack(parse.json) { implicit request =>
        constructResult(addPet(loggedIn))
      }

    case GET(p"/pets/${id}") =>
      AsyncStack { implicit request =>
        constructResult(findPetById(id, loggedIn))
      }

    case DELETE(p"/pets/${id}") =>
      AsyncStack { implicit request =>
        constructResult(deletePet(id, loggedIn))
      }

  }

  def findPets(tags: Option[String], limit: Option[String], loggedIn: Option[User]): HttpResult[Result]
  def addPet(loggedIn: Option[User]): HttpResult[Result]
  def findPetById(id: String, loggedIn: Option[User]): HttpResult[Result]
  def deletePet(id: String, loggedIn: Option[User]): HttpResult[Result]
}
