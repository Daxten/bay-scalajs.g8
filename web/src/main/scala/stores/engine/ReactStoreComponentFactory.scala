package stores.engine

import japgolly.scalajs.react._
import utils.RxObserver

object ReactStoreComponentFactory {

  case class RenderProps2[Id1, T1, Id2, T2](failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T1], Seq[T2]) => ReactElement)

  class RenderBackend2[Id1, T1, Id2, T2]($: BackendScope[RenderProps2[Id1, T1, Id2, T2], Unit], t1: ReactStore[Id1, T1], t2: ReactStore[Id2, T2]) extends RxObserver($) {
    def mounted(p: RenderProps2[Id1, T1, Id2, T2]) = observe(t1.readObs, t2.readObs) >> Callback.future {
      for {
        _ <- t1.smartLoad()
        _ <- t2.smartLoad()
      } yield Callback.empty
    }

    def render(p: RenderProps2[Id1, T1, Id2, T2]): ReactElement = {
      val pots = Seq(t1, t2)
      if (pots.exists(_.now.isPending)) p.pending()
      else if (Seq(t1, t2).forall(_.now.isReady)) p.ready(t1.now.get, t2.now.get)
      else p.failed()
    }
  }

  def build[Id1, T1, Id2, T2](t1: ReactStore[Id1, T1], t2: ReactStore[Id2, T2]) = {
    val name = Seq(t1, t2).map(_.name).mkString("-")

    val component = ReactComponentB[RenderProps2[Id1, T1, Id2, T2]](s"ReactCollection-Combinated-$name")
      .backend(new RenderBackend2(_, t1, t2))
      .render($ => $.backend.render($.props))
      .componentDidMount($ => $.backend.mounted($.props))
      .build

    (failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T1], Seq[T2]) => ReactElement) => component(RenderProps2(failed, pending, ready))
  }
}