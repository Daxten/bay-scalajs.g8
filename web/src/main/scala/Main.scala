import components.LayoutComponent
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfig, RouterConfigDsl}
import models.Locs._
import org.scalajs.dom
import screens.HomeScreen
import shared.utils.UpickleCodecs

import scala.scalajs.js.JSApp

object Main extends JSApp with UpickleCodecs {
  val regExRoute = "[a-zA-Z0-9_-]+"

  def main(): Unit = {
    println("Application starting..")

    val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc]
      .buildConfig { dsl =>
        import dsl._

        (
          staticRoute(root, HomeLoc) ~> renderR(ctl => HomeScreen(ctl).vdomElement)
        ).notFound(redirectToPage(HomeLoc)(Redirect.Replace))
      }
      .renderWith((ctl, res) => LayoutComponent(ctl, res).vdomElement)

    val routerComponent = Router(BaseUrl.fromWindowOrigin_/, routerConfig)

    val appComponent = ScalaComponent.builder[Unit]("App").render(_ => routerComponent().vdomElement).build

    appComponent().renderIntoDOM(dom.document.getElementById("root"))
  }

}
