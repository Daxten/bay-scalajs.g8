package services

import upickle.default._

object AutowireRouter extends autowire.Server[String, Reader, Writer] {
  def write[Result: Writer](r: Result) = upickle.default.write(r)

  def read[Result: Reader](p: String) = upickle.default.read[Result](p)
}
