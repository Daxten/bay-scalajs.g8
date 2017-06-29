package controllers

import cats.implicits._
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

class Application(val controllerComponents: ControllerComponents,
                  val services: Services,
                  security: Security)(implicit val ec: ExecutionContext)
    extends ExtendedController
    with I18nSupport {

  def index(path: String) = security.optUserIdAction { implicit request =>
    request.userId match {
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
    } yield
      Redirect(routes.Application.index("/"))
        .withSession(request.session + (security.sessionKey -> userId.toString))

    constructResult(result)
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.Application.login()).withNewSession
  }

  def api(s: String) = security.UserOptAction(parse.multipartFormData).async { implicit request =>
    val path = s.split("/")
    request.user match {
      case Some(user) =>
        val dataStr = request.body.dataParts
          .get("data")
          .flatMap(_.headOption)
          .getOrElse("")

        AutowireRouter
          .route[WiredApi](new WiredApiService(user, request.body.files, services)) {
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
