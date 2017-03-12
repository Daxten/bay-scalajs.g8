package components

import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import models.Locs.Loc
import shared.models.WiredApiModel.{ApiError, ApiResult}
import shared.utils.Implicits
import utils.HtmlTags

abstract class SimpleApiComponent[T](load: => ApiResult[T]) extends HtmlTags with Implicits {

  type Error = Either[Throwable, ApiError]

  case class Props(c: RouterCtl[Loc])

  case class State(content: Option[T] = None, error: Option[Error] = None)

  def renderLayout(e: TagMod): VdomTag

  def renderLoading($ : BackendScope[Props, State]): VdomTag

  def renderError(e: Error, $ : BackendScope[Props, State]): VdomTag

  def renderContent(content: T, refresh: Callback, $ : BackendScope[Props, State]): VdomTag

  class Backend($ : BackendScope[Props, State]) {

    def mounted(props: Props, state: State): Callback =
      $.modState(
        _.copy(error = None, content = None), {
          Callback.future {
            load.map {
              case Right(e) =>
                $.modState(_.copy(error = None, content = e.asOption))
              case Left(error) =>
                $.modState(_.copy(error = Some(Right(error))))
            } recover {
              case e: Throwable =>
                $.modState(_.copy(error = Some(Left(e)), content = None))
            }
          }
        }
      )

    def render(props: Props, state: State): VdomTag = {
      renderLayout((state.content, state.error) match {
        case (None, None) =>
          renderLoading($)
        case (Some(content), _) =>
          renderContent(content, mounted(props, state), $)
        case (_, Some(error)) =>
          renderError(error, $)
      })
    }
  }

  private val component = ScalaComponent.build[Props]("SimpleFutureComponent")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(e => e.backend.mounted(e.props, e.state))
    .build

  def apply(c: RouterCtl[Loc]): Unmounted[Props, State, Backend] = component(Props(c))
}
