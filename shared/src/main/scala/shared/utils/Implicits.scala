package shared.utils

import scala.concurrent.Future

trait Implicits {
  implicit class ExtGeneric[T](t: T) {
    def asFuture: Future[T] = Future.successful(t)
    def asOption: Option[T] = Option(t)
  }
}

object Implicits extends Implicits
