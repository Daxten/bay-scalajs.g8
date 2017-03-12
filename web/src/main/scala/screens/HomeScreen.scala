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
import utils.HtmlTags
import cats.implicits._

object HomeScreen extends HtmlTags with Codecs {

  case class Props(c: RouterCtl[Loc])

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def mounted() = Callback {
      AjaxClient[WiredApi].now().call().foreach {
        case Right(time) => println(time.toString)
        case Left(e)    => org.scalajs.dom.window.alert(e.toString)
      }
    }

    def render(props: Props, state: State): VdomTag = {
      <.div(
        <.h2("Lorem Ipsum"),
        <.div(
          LoremIpsum.paragraphs(4).map(<.p(_)).toTagMod,
          OffsetDateTime.now.toString
        ),
        <.div(^.marginTop := "40px")(
          LoremIpsumComponent(props.c)
        )
      )
    }
  }

  private val component = ScalaComponent.build[Props]("HomeScreen")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(_.backend.mounted())
    .build

  def apply(c: RouterCtl[Loc]) = component(Props(c))
}
