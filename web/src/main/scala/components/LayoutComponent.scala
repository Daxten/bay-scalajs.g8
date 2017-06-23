package components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import models.Locs.Loc
import _root_.utils.HtmlTags

object LayoutComponent extends HtmlTags {

  case class Props(c: RouterCtl[Loc], r: Resolution[Loc])

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def mounted() = Callback.empty

    def render(props: Props, state: State) = {
      <.div(
        <.div(
          props.r.render()
        ),
        <.div(^.cls := "footer")(
          <.button(^.onClick --> Callback(org.scalajs.dom.window.location.replace("/logout")))("logout")
        )
      )
    }
  }

  private val component =
    ScalaComponent.builder[Props]("LayoutComponent").initialState(State()).renderBackend[Backend].componentDidMount(_.backend.mounted()).build

  def apply(c: RouterCtl[Loc], r: Resolution[Loc]) = component(Props(c, r))
}
