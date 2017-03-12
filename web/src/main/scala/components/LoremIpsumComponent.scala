package components

import autowire._
import japgolly.scalajs.react.{BackendScope, Callback}
import services.AjaxClient
import shared.services.WiredApi

object LoremIpsumComponent extends SimpleApiComponent(AjaxClient[WiredApi].createLoremIpsum().call()) {
  override def renderLoading($ : BackendScope[Props, State]): VdomTag = {
    <.div("Loading ..")
  }

  override def renderError(e: Error, $ : BackendScope[Props, State]): VdomTag = {
    <.div("Error ..")
  }

  override def renderContent(content: List[String], refresh: Callback, $ : BackendScope[Props, State]): VdomTag = {
    <.div(
      <.div(
        content.map(<.p(_)).toTagMod
      ),
      <.button(^.onClick --> refresh)("Refresh")
    )
  }

  override def renderLayout(e: TagMod): VdomTag = <.div(
    <.h2("Using Api"),
    TestComponent(),
    e
  )
}
