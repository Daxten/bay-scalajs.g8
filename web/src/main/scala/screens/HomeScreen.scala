package screens

import java.time.OffsetDateTime

import app.AppRouter.Loc
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.widok.moment.Moment
import shared.models.SharedDefault
import utils.ReactTags

object HomeScreen extends ReactTags {

  case class Props(c: RouterCtl[Loc])

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    import upickle.default._
    import SharedDefault._

    def mounted() = Callback.empty

    def render(props: Props, state: State) = {
      <.div(
        <.h2(SharedDefault.x),
        <.h2(SharedDefault.y),
        <.h2(Moment().format("X")),
        <.h2(OffsetDateTime.now.toEpochSecond),
        <.h2(read[OffsetDateTime](write(OffsetDateTime.now)).toEpochSecond),
        <.h2(OffsetDateTime.now.isBefore(OffsetDateTime.now.minusDays(5)).toString),
        <.h2(OffsetDateTime.now.isAfter(OffsetDateTime.now.minusDays(5)).toString)
      )
    }
  }

  private val component = ReactComponentB[Props]("HomeScreen").initialState(State()).renderBackend[Backend].componentDidMount(_.backend.mounted()).build

  def apply(c: RouterCtl[Loc]) = component(Props(c))
}
