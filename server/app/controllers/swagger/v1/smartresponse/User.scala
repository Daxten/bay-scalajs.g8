package controllers.swagger.v1.smartresponse

import controllers.ExtendedController
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.libs.Files
import play.api.libs.circe._
import play.api.mvc._
import play.api.routing._
import play.api.routing.sird._
import shared.models.swagger.smartresponse.v1._

trait User extends ExtendedController with SimpleRouter with Circe {
  def routes: Router.Routes = {

    case GET(p"/me") =>
      Action.async { implicit request =>
        constructResult(getMe().map(e => Ok(e.asJson)))
      }

    case PUT(p"/me") =>
      Action.async(circe.json[UserData]) { implicit request =>
        constructResult(putMe())
      }

  }

  def getMe()(implicit request: Request[AnyContent]): HttpResult[UserData]
  def putMe()(implicit request: Request[UserData]): HttpResult[Result]
}
