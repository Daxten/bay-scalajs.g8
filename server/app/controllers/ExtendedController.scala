package controllers

import play.api.data.Form
import play.api.mvc.{Controller, Request, Result}
import shared.utils.{Codecs, Implicits}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.{-\/, EitherT, \/, \/-}

/**
  * Created by Haak on 24.04.2016.
  */
trait ExtendedController extends Controller with Implicits with Codecs {
  implicit val ec: ExecutionContext

  // Source: https://github.com/eamelink/flatten/blob/master/play-specific/app/controllers/Part17.scala
  // Type alias for our result type
  type HttpResult[A] = EitherT[Future, Result, A]

  // Constructors for our result type
  object HttpResult {
    def point[A](a: A): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(\/-(a)))
    def fromFuture[A](fa: Future[A]): HttpResult[A] =
      EitherT[Future, Result, A](fa.map(\/-(_)))
    def fromEither[A](va: Result \/ A): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(va))
    def fromEither[A, B](failure: B => Result)(va: B \/ A): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(va.leftMap(failure)))
    def fromOption[A](failure: Result)(oa: Option[A]): HttpResult[A] =
      EitherT[Future, Result, A](Future.successful(oa \/> failure))
    def fromFOption[A](failure: Result)(foa: Future[Option[A]]): HttpResult[A] =
      EitherT[Future, Result, A](foa.map(_ \/> failure))
    def fromFEither[A, B](failure: B => Result)(fva: Future[B \/ A]): HttpResult[A] =
      EitherT[Future, Result, A](fva.map(_.leftMap(failure)))
    def fromForm[FormType](failure: Form[FormType] => Result)(form: Form[FormType])(
        implicit request: Request[_]): HttpResult[FormType] =
      EitherT[Future, Result, FormType](
        form.bindFromRequest.fold(errorForm => -\/(failure(errorForm)).asFuture,
                                  formEntity => \/-(formEntity).asFuture))
  }

  def constructResult(result: HttpResult[Result]): Future[Result] = result.run.map(_.merge)
  def constructResultWithF(result: HttpResult[Future[Result]]): Future[Result] =
    result.run.flatMap(_.leftMap(_.asFuture).merge)
}
