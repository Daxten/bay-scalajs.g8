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

trait Ticket extends ExtendedController with SimpleRouter with Circe {
  def routes: Router.Routes = {

    case GET(p"/last_ticket") =>
      Action.async { implicit request =>
        constructResult(getLast_ticket().map(e => Ok(e.asJson)))
      }

    case POST(p"/tickets") =>
      Action.async(circe.json[ApiTicketInfo]) { implicit request =>
        constructResult(postTickets().map(e => Ok(e.asJson)))
      }

    case PUT(p"/tickets/${ticketId}") =>
      Action.async(circe.json[ApiTicketInfo]) { implicit request =>
        constructResult(putTickets(ticketId))
      }

    case POST(p"/tickets/${ticketId}/media") =>
      Action.async(parse.multipartFormData) { implicit request =>
        constructResult(postTicketsMedia(ticketId))
      }

  }

  def getLast_ticket()(implicit request: Request[AnyContent]): HttpResult[ApiTicket]
  def postTickets()(implicit request: Request[ApiTicketInfo]): HttpResult[ApiTicket]
  def putTickets(ticketId: String)(implicit request: Request[ApiTicketInfo]): HttpResult[Result]
  def postTicketsMedia(ticketId: String)(implicit request: Request[MultipartFormData[Files.TemporaryFile]]): HttpResult[Result]
}
