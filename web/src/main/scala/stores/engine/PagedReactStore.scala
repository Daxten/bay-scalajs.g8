package engine

import diode.data._
import japgolly.scalajs.react.extra.{OnUnmount, TimerSupport}
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactElement}
import shared.models.SharedDefault.SearchResult
import utils.RxObserver

import scala.concurrent.Future

/**
  * Created by Haak on 24.06.2016.
  */
trait PagedReactStore[Id, T] {

  import rx.Ctx.Owner.Unsafe.Unsafe
  import rx._

  def getId(e: T): Id

  def isEqual(a: Id, b: Id): Boolean = a == b

  val name: String

  val query: Var[String] = Var("")

  val count: Var[Int] = Var(0)

  protected val model: Var[Pot[Map[Int, T]]] = Var(Empty)

  def search(query: String, offset: Int, take: Int): Future[SearchResult[T]]

  var timerId: Option[Int] = None

  def canLoadMore = now.size - 1 < count.now

  def loadNext(limit: Int, useCache: Boolean = true) = dependOn(now.getOrElse(Map(-1 -> null)).keys.max + 1, limit, useCache)

  def dependOn(offsetStart: Int, limit: Int, useCache: Boolean = true) = {
    model.update(model.now.pending())

    if (useCache && (offsetStart to offsetStart + limit).forall(id => model.now.getOrElse(Map.empty).keys.exists(_ == id))) {
      println("Skipping Search and using Cache")
    } else {
      search(query.now, offsetStart, limit) onSuccess {
        case SearchResult(result, startOffset, nextOffset, max) =>
          if (count.now != max) count.update(max)

          val current = now.getOrElse(Map.empty)
          val updated = result.foldLeft(current)((b, a) => {
            b + a
          })
          model.update(Ready(updated))
      }
    }

  }

  query.foreach { query =>
    timerId.foreach(org.scalajs.dom.window.clearTimeout)
    model.update(Empty.pending())

    val id = org.scalajs.dom.window.setTimeout(() => {
      search(query, now.getOrElse(Map.empty).keys.lastOption.getOrElse(0), 50) onSuccess {
        case SearchResult(result, startOffset, nextOffset, max) =>
          if (count.now != max) count.update(max)
          val current = now.getOrElse(Map.empty)
          val updated = result.foldLeft(current)((b, a) => {
            b + a
          })
          model.update(Ready(updated))
      }
    }, 1000)

    timerId = Some(id)
  }

  private var runningCallback: Option[Future[Seq[T]]] = None

  // read only access
  def now: Pot[Map[Int, T]] = model.now

  // read only access, observable access
  val readObs: Rx[Pot[Map[Int, T]]] = model.r

  def init = Future.successful(Seq.empty)

  def render(failed: Throwable => ReactElement,
             pending: Long => ReactElement,
             empty: Boolean => ReactElement,
             ready: (Map[Int, T], Boolean) => ReactElement) =
    StoreComponent.component(StoreComponent.Props(failed, pending, empty, ready))

  object StoreComponent {

    import scala.concurrent.duration._

    case class Props(failed: Throwable => ReactElement,
                     pending: Long => ReactElement,
                     empty: Boolean => ReactElement,
                     ready: (Map[Int, T], Boolean) => ReactElement)

    class Backend($ : BackendScope[Props, Unit]) extends RxObserver($) with TimerSupport {
      def mounted(p: Props) =
        dependantObserve[Pot[Map[Int, T]]](
          readObs,
          (a, b) =>
            (a.isReady != b.isReady) || (a.isPending != b.isPending) || (a.isFailed != b.isFailed) || (a.isReady && b.isReady && a.get != b.get)) >>
          setInterval(Callback.when(now.isPending)($.forceUpdate), 1.second)

      def render(p: Props) = {
        now match {
          case Failed(e)                       => p.failed(e)
          case Pending(e) if now.isEmpty       => p.pending(e)
          case Pending(e)                      => p.ready(now.get, true)
          case Ready(e) if e.isEmpty           => p.empty(now.isPending)
          case Ready(e)                        => p.ready(e, now.isPending)
          case FailedStale(t, e)               => null // not used atm
          case PendingStale(t, e) if t.isEmpty => p.empty(true) // TODO: How do we get here?
          case PendingStale(t, e)              => p.ready(t, true) // TODO: How do we get here?
          case Unavailable                     => null // not used atm
          case Empty                           => p.ready(Map.empty, false)
        }
      }
    }

    val component = ReactComponentB[Props](s"$name-Wrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .configure(TimerSupport.install, OnUnmount.install)
      .build
  }
}
