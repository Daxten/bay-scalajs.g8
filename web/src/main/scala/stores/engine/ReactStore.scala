package stores.engine

import diode.data._
import japgolly.scalajs.react.extra.{OnUnmount, TimerSupport}
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactElement}
import utils.RxObserver

import scala.concurrent.Future

trait ReactStore[Id, T] {

  import rx._

  def getId(e: T): Id

  def isEqual(a: Id, b: Id): Boolean = a == b

  val name: String

  def init = Future.successful(Seq.empty[T])

  protected val model: Var[Pot[Seq[T]]] = Var(Empty)
  private var runningCallback: Option[Future[Seq[T]]] = None

  // read only access
  def now: Pot[Seq[T]] = model.now

  // read only access, observable access
  val readObs: Rx[Pot[Seq[T]]] = model.r

  def access: Future[Seq[T]] = accessWith(init)

  def accessWith(f: Future[Seq[T]]): Future[Seq[T]] = {
    (model.now, runningCallback) match {
      case (Failed(e), _)            => Future.failed(e)
      case (_, Some(callback))       => callback
      case (Ready(e), _)             => Future.successful(e)
      case (e, None) if !e.isPending => load(f)
      case (e, None) if e.isPending =>
        println(s"Unexpected Pot State on $name")
        model() = Empty
        load(f)
    }
  }

  def reset: Callback = {
    runningCallback match {
      case Some(callback) =>
        Callback.future {
          callback map { _ =>
            Callback {
              runningCallback = None
              model() = Empty
            }
          }
        }
      case None =>
        Callback {
          runningCallback = None
          model() = Empty
        }
    }
  }

  def refresh: Callback = reset >> Callback(access)

  def refreshWith(f: Future[Seq[T]]): Callback = reset >> Callback(accessWith(f))

  private def load(f: Future[Seq[T]]) = {
    model() = readObs.now.pending()

    println(s"Loading $name")
    runningCallback = Some(f)

    f onSuccess {
      case e =>
        runningCallback = None
        model() = Ready(e)
        e
    }

    f onFailure {
      case e: Throwable =>
        runningCallback = None
        model() = Failed(e)
    }

    f
  }

  def render(failed: Throwable => ReactElement,
             pending: Long => ReactElement,
             empty: () => ReactElement,
             ready: (Seq[T]) => ReactElement) =
    StoreComponent.component(StoreComponent.Props(failed, pending, empty, ready))

  object StoreComponent {

    import scala.concurrent.duration._

    case class Props(failed: Throwable => ReactElement,
                     pending: Long => ReactElement,
                     empty: () => ReactElement,
                     ready: (Seq[T]) => ReactElement)

    class Backend($ : BackendScope[Props, Unit]) extends RxObserver($) with TimerSupport {
      def mounted(p: Props) =
        dependantObserve[Pot[Seq[T]]](readObs, (a, b) =>
          (a.isReady != b.isReady) || (a.isPending != b.isPending) || (a.isFailed != b.isFailed) || (a.isReady == b.isReady && a.headOption != b.headOption)) >>
          setInterval(Callback.when(now.isPending)($.forceUpdate), 1.second) >> Callback(access)

      def render(p: Props) = {
        now match {
          case Failed(e)              => p.failed(e)
          case Pending(e)             => p.pending(e)
          case Ready(e) if e.isEmpty  => p.empty()
          case Ready(e) if e.nonEmpty => p.ready(e)
          case FailedStale(t, e)      => null // not used atm
          case PendingStale(t, e)     => null // not used atm
          case Unavailable            => null // not used atm
          case Empty                  => p.empty()
        }
      }
    }

    val component = ReactComponentB[Props](s"$name-Wrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .configure(TimerSupport.install, OnUnmount.install)
      .build
  }

  def updateOrInsertIntoStore(changed: T): Future[Seq[T]] = {
    for {
      data <- access
    } yield {
      val updatedModel = {
        if (data.exists(e => isEqual(getId(e), getId(changed)))) data.map(x => if (isEqual(getId(changed), getId(x))) changed else x)
        else data :+ changed
      }
      model() = Ready(updatedModel)
      updatedModel
    }
  }

  def updateOrInsertIntoStore(changes: Seq[T]): Future[Seq[T]] = {
    for {
      data <- access
    } yield {
      val updatedModel = changes.foldLeft(data)((coll, e) => {
        coll.map(x => if (isEqual(getId(e), getId(x))) e else x)
      })
      model() = Ready(updatedModel)
      updatedModel
    }
  }

  def removeFromStore(id: Id): Future[Seq[T]] = {
    for {
      data <- access
    } yield {
      val updatedModel = data.filter(e => !isEqual(getId(e), id))
      model() = Ready(updatedModel)
      updatedModel
    }
  }

}
