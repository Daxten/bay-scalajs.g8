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

trait Auth extends ExtendedController with SimpleRouter with Circe {
  def routes: Router.Routes = {

    case POST(p"/auth") =>
      Action.async(circe.json[AuthRequest]) { implicit request =>
        constructResult(postAuth().map(e => Ok(e.asJson)))
      }

  }

  def postAuth()(implicit request: Request[AuthRequest]): HttpResult[AuthKey]
}
