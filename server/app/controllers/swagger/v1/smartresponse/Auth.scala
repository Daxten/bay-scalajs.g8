package controllers.swagger.v1.smartresponse

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
import play.api.libs.Files
import io.circe.generic.auto._
import io.circe.syntax._
import shared.models.swagger.smartresponse.v1._

trait Auth extends ExtendedController with SimpleRouter with Circe {
  def routes: Router.Routes = {

    case POST(p"/auth") =>
      Action.async(circe.json) { implicit request =>
        constructResult(postAuth().map(e => Ok(e.asJson)))
      }

  }

  def postAuth()(implicit request: RequestWithAttributes[Json]): HttpResult[AuthKey]
}
