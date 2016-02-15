package stores.engine

import _root_.reactStore.RxObserver
import diode.data._
import japgolly.scalajs.react._
import utils.RxObserver

import scala.concurrent.Future

trait ReactCollection[T] extends ReactModel {

  import rx._

  val name: String
  protected val model: Var[Pot[Seq[T]]] = Var(Empty)
  private var runningCallback: Option[Future[Seq[T]]] = None

  def asyncLoad: Future[Seq[T]]

  private def load = {
    model() = readObs.now.pending()

    println(s"Loading $name")
    runningCallback = Some(asyncLoad.map { e =>
      model() = Ready(e)
      runningCallback = None
      e
    })

    runningCallback.get
  }

  def smartLoad: Future[Seq[T]] = {
    model.now match {
      case Failed(e) => Future.successful(null) // TODO
      case e if e.isEmpty && !e.isPending => load
      case e if e.isPending => runningCallback.get
      case Ready(e) => Future.successful(e)
    }
  }

  def forceReload = load

  // read only access
  def now = model.now

  // read only access, observable access
  val readObs: Rx[Pot[Seq[T]]] = model.r

  def render(failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T]) => ReactElement) = StoreComponent.component(StoreComponent.Props(failed, pending, ready))

  object StoreComponent {

    case class Props(failed: () => ReactElement, pending: () => ReactElement, ready: (Seq[T]) => ReactElement)

    class Backend($: BackendScope[Props, Unit]) extends RxObserver($) {
      def mounted(p: Props) = observe(model) >> Callback(smartLoad)

      def render(p: Props) = {
        if (now.isPending) p.pending()
        else if (now.isReady) p.ready(now.get)
        else p.failed()
      }
    }

    val component = ReactComponentB[Props](s"$name-Wrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .build
  }

}