package controllers

import com.google.inject.Inject
import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services._
import io.circe.JsonObject

import scala.concurrent.ExecutionContext

class Application @Inject()(val messagesApi: MessagesApi)(implicit val ec: ExecutionContext)
    extends ExtendedController
    with AuthConfigImpl
    with OptionalAuthElement
    with LoginLogout
    with I18nSupport {

  import io.circe.parser._
  import io.circe.syntax._
  import shared.models.SharedDefault._

  def index(path: String) = StackAction { implicit request =>
    loggedIn match {
      case Some(user) =>
        Ok(views.html.index())
      case None =>
        Ok(views.html.login())
    }
  }

  def login = Action.async { implicit request =>
    gotoLoginSucceeded(1)
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def api(s: String) = AsyncStack(parse.json) { implicit request =>
    val path = s.split("/")
    AutowireRouter.route[Api](new ApiService(loggedIn)) {
      val routed = for {
        json <- io.circe.parser.parse(request.body.toString()).right
        mapped <- json.as[Map[String, String]].right
      } yield autowire.Core.Request(path, mapped)

      routed.right.get
    } map { responseData =>
      Ok(responseData)
    }
  }

}
