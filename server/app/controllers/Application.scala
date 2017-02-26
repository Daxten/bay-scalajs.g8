package controllers

import com.google.inject.Inject
import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import models.forms.LoginForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import services._
import cats.implicits._
import services.dao.UserDao
import shared.services.WiredApi
import upickle.default._

import scala.concurrent.ExecutionContext

class Application @Inject()(val messagesApi: MessagesApi, val userDao: UserDao)(implicit val ec: ExecutionContext)
    extends ExtendedController
    with AuthConfigImpl
    with OptionalAuthElement
    with LoginLogout
    with I18nSupport {

  def index(path: String) = StackAction { implicit request =>
    loggedIn match {
      case Some(user) =>
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
      form <- loginForm.bindFromRequest()       |> HttpResult.fromForm(e => BadRequest(views.html.login(e)))
      userId <- userDao.maybeLogin(form)        |> HttpResult.fromFOption(BadRequest(views.html.login(loginForm.fill(form).withGlobalError("bad.password"))))
      loginResult <- gotoLoginSucceeded(userId) |> HttpResult.fromFuture
    } yield loginResult

    constructResult(result)
  }

  def logout: Action[AnyContent] = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def api(s: String): Action[JsValue] = AsyncStack(parse.json) { implicit request =>
    val path = s.split("/")
    AutowireRouter.route[WiredApi](new WiredApiService(loggedIn)) {
      val json = read[Map[String, String]](request.body.toString())
      autowire.Core.Request(path, json)
    } map { responseData =>
      Ok(responseData)
    }
  }
}
