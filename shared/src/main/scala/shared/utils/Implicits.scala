package shared.utils

import shared.models.WiredApiModel.ApiResult

import scala.concurrent.Future

trait Implicits {

  implicit class ExtGeneric[T](t: T) {

    def asFuture: Future[T] = Future.successful(t)

    def asOption: Option[T] = Option(t)

    def asResult: ApiResult[T] = Future.successful(Right(t))

    def |>[R](f: T => R): R = f(t)

    def condOption(condition: Boolean) = if (condition) Some(t) else None
  }

}

object Implicits extends Implicits
