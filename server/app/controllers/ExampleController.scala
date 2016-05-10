package controllers

import play.api.i18n.MessagesApi
import play.api.mvc.Action
import shared.models.SharedDefault.BaseUser

import scala.concurrent.{ExecutionContext, Future}
import scalaz._
import scalaz.Scalaz._

/**
  * Created by Haak on 24.04.2016.
  */
class ExampleController @com.google.inject.Inject()(val messagesApi: MessagesApi)(implicit val ec: ExecutionContext) extends ExtendedController {

  def validateEmail(email: String): String \/ String =
    if (email contains "@") \/-(email)
    else -\/("Not a valid email address: " + email)

  def index = Action.async { request =>
    for {
      username <- Some("username")                              |> HttpResult.fromOption(BadRequest("Username missing from request"))
      user     <- Future.successful(Some(BaseUser(1, "aname"))) |> HttpResult.fromFOption(NotFound("User not found"))
      email = s"${user.name}@emailprovider.de"
      validatedEmail <- validateEmail(email)    |> HttpResult.fromEither(InternalServerError(_))
      success        <- Future.successful(true) |> HttpResult.fromFuture
    } yield {
      if (success) Ok("Mail successfully sent!")
      else InternalServerError("Failed to send email :(")
    }
  }
}
