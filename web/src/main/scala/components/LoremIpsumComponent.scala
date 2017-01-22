package components

import autowire._
import japgolly.scalajs.react.{BackendScope, Callback}
import services.{AjaxClient, Api}

object LoremIpsumComponent extends SimpleApiComponent(AjaxClient[Api].createLoremIpsum().call()) {
  override def renderLoading($ : BackendScope[Props, State]): ReactTag = {
    <.div("Loading ..")
  }

  override def renderError(e: Error, $ : BackendScope[Props, State]): ReactTag = {
    <.div("Error ..")
  }

  override def renderContent(content: List[String], refresh: Callback, $ : BackendScope[Props, State]): ReactTag = {
    <.div(
      <.div(
        content.map(<.p(_))
      ),
      <.button(^.onClick --> refresh)("Refresh")
    )
  }

  override def renderLayout(e: TagMod): ReactTag = <.div(
    <.h2("Using Api"),
    TestComponent(),
    e
  )
}
