package components

import japgolly.scalajs.react.extra.LogLifecycle
import japgolly.scalajs.react.{ReactComponentB, ReactComponentU, TopNode}
import utils.ReactTags

object TestComponent extends ReactTags {

  private val component = ReactComponentB[Unit]("TestComponent")
    .render(_ => <.div("I'm only here to log my lifecycle."))
    .configure(LogLifecycle.short)
    .build

  def apply(): ReactComponentU[Unit, Unit, Unit, TopNode] = component()
}
