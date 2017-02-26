package controllers.swagger.v1.smartresponse

import com.google.inject.Inject
import io.circe.Json
import play.api.mvc.Request
import shared.models.swagger.smartresponse.v1.{AuthKey, AuthRequest}

import scala.concurrent.ExecutionContext
import scalaz.{-\/, \/}

class AuthRouter @Inject()()(implicit val ec: ExecutionContext) extends Auth {
  override def postAuth()(implicit request: Request[AuthRequest]): HttpResult[AuthKey] = ???
}
