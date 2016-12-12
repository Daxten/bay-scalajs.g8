package services

import org.scalajs.dom
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

import scala.concurrent.Future

object AjaxClient extends autowire.Client[String, Decoder, Encoder] {

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def doCall(req: Request): Future[String] = {

    dom.ext.Ajax
      .post(
        url = "/api/" + req.path.mkString("/"),
        data = req.args.asJson.noSpaces,
        headers = Map("Content-Type" -> "application/json")
      )
      .map(_.responseText)
  }

  def write[Result: Encoder](r: Result) = r.asJson.noSpaces

  def read[Result: Decoder](p: String) = parse(p) match {
    case Left(e) => throw e
    case Right(e) =>
      e.as[Result] match {
        case Left(e)  => throw e
        case Right(e) => e
      }
  }
}
