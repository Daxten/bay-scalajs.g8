package services

import org.scalajs.dom
import scala.concurrent.Future

object AjaxClient extends autowire.Client[String, upickle.default.Reader, upickle.default.Writer] {

  override def doCall(req: Request): Future[String] = {

    dom.ext.Ajax
      .post(
        url = "/wired/" + req.path.mkString("/"),
        data = upickle.default.write(req.args),
        headers = Map("Content-Type" -> "application/json")
      )
      .map(_.responseText)
  }

  def write[Result: upickle.default.Writer](r: Result) = upickle.default.write(r)

  def read[Result: upickle.default.Reader](p: String) = upickle.default.read[Result](p)
}
