import scala.concurrent.Future
import scala.language.implicitConversions

/**
  * Created by Haak on 24.04.2016.
  */
package object services {
  implicit def toFuture[T](e: T): Future[T] = Future.successful(e)
}
