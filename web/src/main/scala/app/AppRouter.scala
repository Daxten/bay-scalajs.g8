package app

import components.LayoutComponent
import japgolly.scalajs.react.extra.router._
import screens._

object AppRouter {
  val regExRoute = "[a-zA-Z0-9_-]+"

  // Define Locations inside the App
  sealed trait Loc

  case object HomeLoc extends Loc

  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    (
      staticRoute(root, HomeLoc) ~> renderR(ctl => HomeScreen(ctl))
      ).notFound(redirectToPage(HomeLoc)(Redirect.Replace))
  }
    .renderWith(LayoutComponent.apply)

  val component = Router(BaseUrl.fromWindowOrigin_/, routerConfig)
}
