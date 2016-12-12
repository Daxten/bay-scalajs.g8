package screens

import app.AppRouter.Loc
import autowire._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import services.{AjaxClient, Api}
import utils.ReactTags
import shared.models.SharedDefault._

object HomeScreen extends ReactTags {

  case class Props(c: RouterCtl[Loc])

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def mounted() = Callback {
      AjaxClient[Api].now().call().foreach(time => println(time.toString()))
    }

    def render(props: Props, state: State) = {
      <.div(
        <.h3("Homescreen")
      )
    }
  }

  private val component = ReactComponentB[Props]("HomeScreen").initialState(State()).renderBackend[Backend].componentDidMount(_.backend.mounted()).build

  def apply(c: RouterCtl[Loc]) = component(Props(c))
}
