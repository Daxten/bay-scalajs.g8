package utils

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount

abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {

  import rx._

  protected def observe[T](rxs: Rx[T]*): Callback = Callback {
    rxs.foreach { rx =>
      val obs = rx.foreach(_ => {
        scope.forceUpdate.runNow
      })
      onUnmount(Callback(obs.kill))
    }
  }
}