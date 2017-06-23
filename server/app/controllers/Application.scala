package controllers

import cats.implicits._
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.LoginLogout
import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.forms.LoginForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import services._
import shared.models.WiredApiModel.UnauthorizedApi
import shared.services.WiredApi
import upickle.default._

import scala.concurrent.ExecutionContext

class Application @Inject()(val services: Services)(implicit val ec: ExecutionContext)
    extends ExtendedController
    with AuthConfigImpl
    with OptionalAuthElement
    with LoginLogout
    with I18nSupport {

  def index(path: String) = StackAction { implicit request =>
    loggedIn match {
      case Some(_) =>
        Ok(views.html.index())
      case None =>
        Ok(views.html.login(loginForm.fill(LoginForm("test@test.de", "testpw"))))
    }
  }

  val loginForm = Form(
    mapping(
      "email"    -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

  def login: Action[AnyContent] = Action.async { implicit request =>
    val result = for {
      form <- loginForm.bindFromRequest() |> HttpResult.fromForm(e =>
        BadRequest(views.html.login(e)))
      userId <- services.userDao.maybeLogin(form) |> HttpResult.fromFOption(
        BadRequest(views.html.login(loginForm.fill(form).withGlobalError("bad.password"))))
      loginResult <- gotoLoginSucceeded(userId) |> HttpResult.fromFuture
    } yield loginResult

    constructResult(result)
  }

  def logout: Action[AnyContent] = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def api(s: String) = AsyncStack(parse.multipartFormData) { implicit request =>
    val path = s.split("/")
    loggedIn match {
      case Some(user) =>
        val dataStr = request.body.dataParts
          .get("data")
          .flatMap(_.headOption)
          .getOrElse("")

        AutowireRouter
          .route[WiredApi](new WiredApiService(user, services, request.body.files)) {
            val json = read[Map[String, String]](dataStr)
            autowire.Core.Request(path, json)
          }
          .map { responseData =>
            Ok(responseData).as("application/json")
          }
      case None =>
        Ok(write(Left(UnauthorizedApi))).as("application/json").asFuture
    }
  }
}
