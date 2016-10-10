package components

import java.time.OffsetDateTime

import app.AppRouter.Loc
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import utils.ReactTags

object LayoutComponent extends ReactTags {

  case class Props(c: RouterCtl[Loc], r: Resolution[Loc])

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def mounted() = Callback.empty

    def render(props: Props, state: State) = {
      <.div(
        <.h1("Layout"),
        <.div(
          <.h2("Render Content:"),
          props.r.render()
        )
      )
    }
  }

  private val component = ReactComponentB[Props]("LayoutComponent").initialState(State()).renderBackend[Backend].componentDidMount(_.backend.mounted()).build

  def apply(c: RouterCtl[Loc], r: Resolution[Loc]) = component(Props(c, r))
}
