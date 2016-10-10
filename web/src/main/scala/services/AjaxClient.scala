package services

import org.scalajs.dom
import upickle.default._

import scala.concurrent.Future

object AjaxClient extends autowire.Client[String, Reader, Writer] {

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def doCall(req: Request): Future[String] = {

    dom.ext.Ajax
      .post(
        url = "/api/" + req.path.mkString("/"),
        data = upickle.default.write(req.args),
        headers = Map("Content-Type" -> "application/json")
      )
      .map(_.responseText)
  }

  def write[Result: Writer](r: Result) = upickle.default.write(r)

  def read[Result: Reader](p: String) = upickle.default.read[Result](p)
}
