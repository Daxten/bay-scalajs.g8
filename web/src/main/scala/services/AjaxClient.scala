package services

import org.scalajs.dom
import org.scalajs.dom.FormData
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.{File, XMLHttpRequest}

import scala.concurrent.{Future, Promise}

class AjaxClient(files: Map[String, File])
  extends autowire.Client[String,
    upickle.default.Reader,
    upickle.default.Writer] {

  override def doCall(req: Request): Future[String] = {

    val promise = Promise[XMLHttpRequest]
    val xhr     = new dom.XMLHttpRequest
    xhr.onreadystatechange = (e: dom.Event) => {
      if (xhr.readyState == dom.XMLHttpRequest.DONE) {
        promise.success(xhr)
      }
    }

    xhr.onerror = { e: dom.ErrorEvent =>
      promise.failure(AjaxException(xhr))
    }

    //start upload
    val formData = new FormData()

    formData.append("data", upickle.default.write(req.args))
    files.foreach {
      case (key, file) => formData.append(key, file)
    }
    xhr.open("POST", "/wired/" + req.path.mkString("/"), true)
    xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest")
    xhr.send(formData)

    promise.future.map(_.responseText)
  }

  def write[Result: upickle.default.Writer](r: Result): String =
    upickle.default.write(r)

  def read[Result: upickle.default.Reader](p: String): Result =
    upickle.default.read[Result](p)
}

object AjaxClient {
  def apply[Trait]                           = new AjaxClient(Map.empty)[Trait]
  def apply[Trait](files: Map[String, File]) = new AjaxClient(files)[Trait]
  def apply[Trait](files: (String, File)*)   = new AjaxClient(files.toMap)[Trait]
}

