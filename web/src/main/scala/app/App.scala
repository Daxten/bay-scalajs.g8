package app

import japgolly.scalajs.react._
import org.scalajs.dom
import services.Styles

import scala.scalajs.js.JSApp
import scalacss.Defaults._
import scalacss.ScalaCssReact._

object App extends JSApp {

  val component =
    ReactComponentB[Unit]("App")
      .render(_ => AppRouter.component())
      .buildU
  
  def main(): Unit = {
    println("Application starting..")
    Styles.addToDocument()
    Styles.loadGlobal
    ReactDOM.render(component(), dom.document.getElementById("root"))
  }
}
