package app

import japgolly.scalajs.react._
import org.scalajs.dom

import scala.scalajs.js.JSApp

object App extends JSApp {

  val component =
    ReactComponentB[Unit]("App").render(_ => AppRouter.component()).build

  def main(): Unit = {
    println("Application starting..")
    ReactDOM.render(component(), dom.document.getElementById("root"))
  }

}
