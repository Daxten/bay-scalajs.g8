package controllers

import cats.data.EitherT
import play.api.data.Form
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import shared.utils.Codecs
import shared.utils.Implicits
import cats.syntax.either._
import play.api.i18n.MessagesApi
import services.Services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
  * Created by Haak on 24.04.2016.
  */
trait ExtendedController extends Controller with Implicits with Codecs {
  val services: Services
  implicit val ec: ExecutionContext

  type HttpResult[A] = EitherT[Future, Result, A]

  // Constructors for our result type
  object HttpResult {

    def point[A](a: A): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(Right(a)))

    def fromFuture[A](fa: Future[A]): HttpResult[A] =
      EitherT[Future, Result, A](fa.map(Right(_)))

    def fromEither[A](va: Either[Result, A]): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(va))

    def fromEither[A, B](failure: B => Result)(va: Either[B, A]): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(va.leftMap(failure)))

    def fromOption[A](failure: Result)(oa: Option[A]): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(oa.toRight(failure)))

    def fromFOption[A](failure: Result)(foa: Future[Option[A]]): HttpResult[A] =
      EitherT[Future, Result, A](foa.map(_.toRight(failure)))

    def fromFEither[A, B](failure: B => Result)(fva: Future[Either[B, A]]): HttpResult[A] =
      EitherT[Future, Result, A](fva.map(_.leftMap(failure)))

    def fromForm[FormType](failure: Form[FormType] => Result)(form: Form[FormType])(
        implicit request: Request[_]): HttpResult[FormType] =
      EitherT[Future, Result, FormType](
        form.bindFromRequest.fold(errorForm => Left(failure(errorForm)).asFuture,
                                  formEntity => Right(formEntity).asFuture))
  }

  def constructResult(result: HttpResult[Result]): Future[Result] = result.value.map(_.merge)

  implicit class ExtResult(e: Result) {

    def pureResult: HttpResult[Result] =
      EitherT[Future, Result, Result](Future.successful(Right(e)))
  }

  implicit class EnrichedOps[T](t: T) {
    def |>[R](f: T => R): R = f(t)
  }

  def messagesApi: MessagesApi = services.messagesApi
}
