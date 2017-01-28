package utils

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount

abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {

  import rx._
  import Ctx.Owner.Unsafe._

  protected def observe[T](rxs: Rx[T]*): Callback = Callback {
    rxs.foreach { rx =>
      val obs = rx.triggerLater {
        scope.forceUpdate.runNow
      }
      onUnmount(Callback(obs.kill))
    }
  }

  protected def dependantObserve[T](rx: Rx[T], shouldUpdate: (T, T) => Boolean): Callback = {
    val obs = rx.reduce((a, b) => {
      if (shouldUpdate(a, b)) {
        scope.forceUpdate.runNow
      }
      a
    })
    onUnmount(Callback(obs.kill()))
  }
}
