package app

import java.time.OffsetDateTime

import japgolly.scalajs.react._
import org.scalajs.dom
import services.{AjaxClient, Api}

import scala.scalajs.js.JSApp

object App extends JSApp {

  val component =
    ReactComponentB[Unit]("App")
      .render(_ => AppRouter.component())
      .buildU

  def main(): Unit = {
    println("Application starting..")
    ReactDOM.render(component(), dom.document.getElementById("root"))

    import autowire._
    import scalajs.concurrent.JSExecutionContext.Implicits.queue

    AjaxClient[Api].now().call().map { now =>
      println(OffsetDateTime.now.toEpochSecond)
      println(now.toEpochSecond)
    }
  }


}
