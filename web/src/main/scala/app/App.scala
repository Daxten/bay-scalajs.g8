package app

import japgolly.scalajs.react._
import org.scalajs.dom
import scala.scalajs.js.JSApp

object App extends JSApp {

  private val component = ScalaComponent.build[Unit]("App").render(_ => AppRouter.component().vdomElement).build

  def main(): Unit = {
    println("Application starting..")

    component().renderIntoDOM(dom.document.getElementById("root"))
  }

}
