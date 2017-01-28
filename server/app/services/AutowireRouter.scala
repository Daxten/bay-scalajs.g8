package services

import shared.utils.UpickleCodecs

object AutowireRouter extends autowire.Server[String, upickle.default.Reader, upickle.default.Writer] with UpickleCodecs {
  def write[Result: upickle.default.Writer](r: Result): String = upickle.default.write(r)

  def read[Result: upickle.default.Reader](p: String): Result = upickle.default.read[Result](p)
}
