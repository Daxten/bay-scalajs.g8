package screens

import app.AppRouter.Loc
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import utils.ReactTags

object HomeScreen extends ReactTags {

  case class Props(c: RouterCtl[Loc])

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def mounted() = Callback.empty

    def render(props: Props, state: State) = {
      <.div(
        <.h2("I'm home")
      )
    }
  }

  private val component = ReactComponentB[Props]("HomeScreen")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(_.backend.mounted())
    .build

  def apply(c: RouterCtl[Loc]) = component(Props(c))
}
