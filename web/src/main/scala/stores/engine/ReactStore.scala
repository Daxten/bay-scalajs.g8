package stores.engine

import diode.data._
import japgolly.scalajs.react.extra.TimerSupport
import japgolly.scalajs.react.{Callback, BackendScope, ReactComponentB, ReactElement}
import utils.RxObserver

import scala.concurrent.Future

trait ReactStore[Id, T] {

  import rx._

  def getId(e: T): Id

  def isEqual(a: Id, b: Id): Boolean = a == b

  val name: String

  def initData = Future.successful(Seq.empty[T])

  protected val model: Var[Pot[Seq[T]]] = Var(Empty)
  private var runningCallback: Option[Future[Seq[T]]] = None

  def init = Future.successful(Seq.empty)

  private def load(f: Future[Seq[T]]) = {
    model() = readObs.now.pending()

    println(s"Loading $name")
    runningCallback = Some(f.map { e =>
      model() = Ready(e)
      runningCallback = None
      e
    })

    f recover {
      case e: Throwable =>
        model() = Failed(e)
        runningCallback = None
    }

    runningCallback.get
  }

  def smartLoad(f: Future[Seq[T]] = initData): Future[Seq[T]] = {
    model.now match {
      case Failed(e) => Future.failed(e)
      case e if e.isPending => runningCallback.get
      case e if !e.isPending => load(f)
    }
  }

  // read only access
  def now = model.now

  // read only access, observable access
  val readObs: Rx[Pot[Seq[T]]] = model.r

  def render(failed: Throwable => ReactElement, pending: Long => ReactElement, ready: (Seq[T]) => ReactElement) = StoreComponent.component(StoreComponent.Props(failed, pending, ready))

  object StoreComponent {
    import scala.concurrent.duration._

    case class Props(failed: Throwable => ReactElement, pending: Long => ReactElement, ready: (Seq[T]) => ReactElement)

    class Backend($: BackendScope[Props, Unit]) extends RxObserver($) with TimerSupport {
      def mounted(p: Props) = dependantObserve[Pot[Seq[T]]](readObs, (a, b) => (a.isReady != b.isReady) || (a.isPending != b.isPending) || (a.isFailed != b.isFailed) || (a.isReady && b.isReady && a.get != b.get)) >>
        setInterval(Callback.ifTrue(now.isPending, $.forceUpdate), 1.second)

      def render(p: Props) = {
        now match {
          case Failed(e) => p.failed(e)
          case Pending(e) => p.pending(e)
          case Ready(e) => p.ready(e)
        }
      }
    }

    val component = ReactComponentB[Props](s"$name-Wrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .configure(TimerSupport.install)
      .build
  }

  def updateOrInsertIntoStore(changed: T): Future[Seq[T]] = {
    for {
      data <- smartLoad()
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
      data <- smartLoad()
    } yield {
      val updatedModel = changes.foldLeft(data)((coll, e) => {
        coll.find(x => isEqual(getId(e), getId(x))).fold {
          coll :+ e
        } { _ =>
          coll.map(x => if (isEqual(getId(e), getId(x))) e else x)
        }
      })
      model() = Ready(updatedModel)
      updatedModel
    }
  }

  def removeFromStore(id: Id): Future[Seq[T]] = {
    for {
      data <- smartLoad()
    } yield {
      val updatedModel = data.filter(e => !isEqual(getId(e), id))
      model() = Ready(updatedModel)
      updatedModel
    }
  }

}
