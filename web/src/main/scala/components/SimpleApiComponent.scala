package components

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactComponentU, TopNode}
import models.Locs.Loc
import shared.models.ApiModel.{ApiError, ApiResult}
import shared.utils.Implicits
import utils.ReactTags

import scalaz.{-\/, \/-}

abstract class SimpleApiComponent[T](load: => ApiResult[T]) extends ReactTags with Implicits {

  type Error = Either[Throwable, ApiError]

  case class Props(c: RouterCtl[Loc])

  case class State(content: Option[T] = None, error: Option[Error] = None)

  def renderLayout(e: TagMod): ReactTag

  def renderLoading($ : BackendScope[Props, State]): ReactTag

  def renderError(e: Error, $ : BackendScope[Props, State]): ReactTag

  def renderContent(content: T, refresh: Callback, $ : BackendScope[Props, State]): ReactTag

  class Backend($ : BackendScope[Props, State]) {

    def mounted(props: Props, state: State): Callback =
      $.modState(
        _.copy(error = None, content = None), {
          Callback.future {
            load.map {
              case \/-(e) =>
                $.modState(_.copy(error = None, content = e.asOption))
              case -\/(error) =>
                $.modState(_.copy(error = Some(Right(error))))
            } recover {
              case e: Throwable =>
                $.modState(_.copy(error = Some(Left(e)), content = None))
            }
          }
        }
      )

    def render(props: Props, state: State): ReactTag = {
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

  private val component = ReactComponentB[Props]("SimpleFutureComponent")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(e => e.backend.mounted(e.props, e.state))
    .build

  def apply(c: RouterCtl[Loc]): ReactComponentU[Props, State, Backend, TopNode] = component(Props(c))
}
