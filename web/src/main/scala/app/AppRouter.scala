package app

import components.LayoutComponent
import japgolly.scalajs.react.extra.router._
import models.Locs._
import screens._

object AppRouter {
  val regExRoute = "[a-zA-Z0-9_-]+"

  val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc]
    .buildConfig { dsl =>
      import dsl._

      (
        staticRoute(root, HomeLoc) ~> renderR(ctl => HomeScreen(ctl).vdomElement)
      ).notFound(redirectToPage(HomeLoc)(Redirect.Replace))
    }
    .renderWith((ctl, res) => LayoutComponent(ctl, res).vdomElement)

  val component = Router(BaseUrl.fromWindowOrigin_/, routerConfig)
}
