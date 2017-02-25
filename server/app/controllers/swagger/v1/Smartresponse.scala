package controllers.swagger.v1

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

trait Smartresponse extends ExtendedController with SimpleRouter with Circe {
  def routes: Router.Routes = {

    case POST(p"/auth") =>
      Action.async(circe.json) { implicit request =>
        constructResult(postAuth().map(e => Ok(e.asJson)))
      }

    case GET(p"/last_ticket" ? q"authkey=$authkey") =>
      Action.async { implicit request =>
        constructResult(getLast_ticket(authkey).map(e => Ok(e.asJson)))
      }

    case POST(p"/tickets" ? q"authkey=$authkey") =>
      Action.async(circe.json) { implicit request =>
        constructResult(postTickets(authkey).map(e => Ok(e.asJson)))
      }

    case PUT(p"/tickets/${ticketId}" ? q"authkey=$authkey") =>
      Action.async(circe.json) { implicit request =>
        constructResult(putTickets(authkey, ticketId))
      }

    case POST(p"/tickets/${ticketId}/media" ? q"authkey=$authkey") =>
      Action.async(parse.multipartFormData) { implicit request =>
        constructResult(postTicketsMedia(ticketId, authkey))
      }

    case GET(p"/me" ? q"authkey=$authkey") =>
      Action.async { implicit request =>
        constructResult(getMe(authkey).map(e => Ok(e.asJson)))
      }

    case PUT(p"/me" ? q"authkey=$authkey") =>
      Action.async(circe.json) { implicit request =>
        constructResult(putMe(authkey))
      }

  }

  def postAuth()(implicit request: RequestWithAttributes[Json]): HttpResult[AuthKey]
  def getLast_ticket(authkey: String)(implicit request: RequestWithAttributes[AnyContent]): HttpResult[ApiTicket]
  def postTickets(authkey: String)(implicit request: RequestWithAttributes[Json]): HttpResult[ApiTicket]
  def putTickets(authkey: String, ticketId: String)(implicit request: RequestWithAttributes[Json]): HttpResult[Result]
  def postTicketsMedia(ticketId: String, authkey: String)(implicit request: RequestWithAttributes[MultipartFormData[Files.TemporaryFile]]): HttpResult[Result]
  def getMe(authkey: String)(implicit request: RequestWithAttributes[AnyContent]): HttpResult[UserData]
  def putMe(authkey: String)(implicit request: RequestWithAttributes[Json]): HttpResult[Result]
}
