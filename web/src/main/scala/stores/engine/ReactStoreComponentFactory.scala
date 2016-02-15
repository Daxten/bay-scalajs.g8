package stores.engine

import japgolly.scalajs.react._
import utils.RxObserver

/**
  * Created by Alexej on 10.02.2016.
  */
object ReactStoreComponentFactory {

  case class RenderProps2[T1, T2](failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T1], Seq[T2]) => ReactElement)

  class RenderBackend2[T1, T2]($: BackendScope[RenderProps2[T1, T2], Unit], t1: ReactCollection[T1], t2: ReactCollection[T2]) extends RxObserver($) {
    def mounted(p: RenderProps2[T1, T2]) = observe(t1.readObs, t2.readObs) >> Callback.future {
      for {
        _ <- t1.smartLoad
        _ <- t2.smartLoad
      } yield Callback.empty
    }

    def render(p: RenderProps2[T1, T2]): ReactElement = {
      val pots = Seq(t1, t2)
      if (pots.exists(_.now.isPending)) p.pending()
      else if (Seq(t1, t2).forall(_.now.isReady)) p.ready(t1.now.get, t2.now.get)
      else p.failed()
    }
  }

  def build[T1, T2](t1: ReactCollection[T1], t2: ReactCollection[T2]) = {
    val name = Seq(t1, t2).map(_.name).mkString("-")

    val component = ReactComponentB[RenderProps2[T1, T2]](s"ReactCollection-Combinated-$name")
      .backend(new RenderBackend2(_, t1, t2))
      .render($ => $.backend.render($.props))
      .componentDidMount($ => $.backend.mounted($.props))
      .build

    (failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T1], Seq[T2]) => ReactElement) => component(RenderProps2(failed, pending, ready))
  }

  case class RenderProps3[T1, T2, T3](failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T1], Seq[T2], Seq[T3]) => ReactElement)

  class RenderBackend3[T1, T2, T3]($: BackendScope[RenderProps3[T1, T2, T3], Unit], t1: ReactCollection[T1], t2: ReactCollection[T2], t3: ReactCollection[T3]) extends RxObserver($) {
    def mounted(p: RenderProps3[T1, T2, T3]) = observe(t1.readObs, t2.readObs, t3.readObs) >> Callback.future {
      for {
        _ <- t1.smartLoad
        _ <- t2.smartLoad
        _ <- t3.smartLoad
      } yield Callback.empty
    }

    def render(p: RenderProps3[T1, T2, T3]) = {
      val pots = Seq(t1, t2, t3)
      if (pots.exists(_.now.isPending)) p.pending()
      else if (Seq(t1, t2).forall(_.now.isReady)) p.ready(t1.now.get, t2.now.get, t3.now.get)
      else p.failed()
    }
  }

  def build[T1, T2, T3](t1: ReactCollection[T1], t2: ReactCollection[T2], t3: ReactCollection[T3]) = {
    val name = Seq(t1, t2, t3).map(_.name).mkString("-")

    val component = ReactComponentB[RenderProps3[T1, T2, T3]](s"ReactCollection-Combinated-$name")
      .backend(new RenderBackend3(_, t1, t2, t3))
      .render($ => $.backend.render($.props))
      .componentDidMount($ => $.backend.mounted($.props))
      .build

    (failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T1], Seq[T2], Seq[T3]) => ReactElement) => component(RenderProps3(failed, pending, ready))
  }
}