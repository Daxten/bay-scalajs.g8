package components

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.LogLifecycle
import utils.HtmlTags

object TestComponent extends HtmlTags {

  private val component = ScalaComponent.build[Unit]("TestComponent")
    .render(_ => <.div("I'm only here to log my lifecycle."))
    .configure(LogLifecycle.short)
    .build

  def apply() = component()
}
