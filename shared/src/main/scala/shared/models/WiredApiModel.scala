package shared.models

import scala.concurrent.Future

object WiredApiModel {
  sealed trait ApiError
  case object NotFound extends ApiError
  case object UnauthorizedApi extends ApiError

  case object NoContent

  type ApiResult[T] = Future[Either[ApiError, T]]
}
