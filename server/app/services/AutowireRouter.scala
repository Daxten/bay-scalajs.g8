package services

import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

object AutowireRouter extends autowire.Server[String, Decoder, Encoder] {
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
