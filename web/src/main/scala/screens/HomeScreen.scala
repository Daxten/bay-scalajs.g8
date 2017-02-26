package screens

import java.time.OffsetDateTime

import autowire._
import components.LoremIpsumComponent
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import models.Locs.Loc
import services.AjaxClient
import shared.services.WiredApi
import shared.utils.{Codecs, LoremIpsum}
import utils.ReactTags
import cats.implicits._

object HomeScreen extends ReactTags with Codecs {

  case class Props(c: RouterCtl[Loc])

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def mounted() = Callback {
      AjaxClient[WiredApi].now().call().foreach {
        case Right(time) => println(time.toString)
        case Left(e)    => org.scalajs.dom.window.alert(e.toString)
      }
    }

    def render(props: Props, state: State): ReactTag = {
      <.div(
        <.h2("Lorem Ipsum"),
        <.div(
          LoremIpsum.paragraphs(4).map(<.p(_)),
          OffsetDateTime.now.toString
        ),
        <.div(^.marginTop := "40px")(
          LoremIpsumComponent(props.c)
        )
      )
    }
  }

  private val component = ReactComponentB[Props]("HomeScreen")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(_.backend.mounted())
    .build

  def apply(c: RouterCtl[Loc]): ReactComponentU[Props, State, Backend, TopNode] = component(Props(c))
}
