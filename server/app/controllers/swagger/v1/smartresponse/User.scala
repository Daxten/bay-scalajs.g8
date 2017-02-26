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
        val optApiKey = request.headers.get("api_key")
        optApiKey match {
          case None => Unauthorized.asFuture
          case Some(apiKey) =>
            constructResult(getMe(apiKey).map(e => Ok(e.asJson)))
        }
      }

    case PUT(p"/me") =>
      Action.async(circe.json[UserData]) { implicit request =>
        val optApiKey = request.headers.get("api_key")
        optApiKey match {
          case None => Unauthorized.asFuture
          case Some(apiKey) =>
            constructResult(putMe(apiKey))
        }
      }

  }

  def getMe(apiKey: String)(implicit request: Request[AnyContent]): HttpResult[UserData]
  def putMe(apiKey: String)(implicit request: Request[UserData]): HttpResult[Result]
}
