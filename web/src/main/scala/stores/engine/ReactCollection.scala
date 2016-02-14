package stores.engine

import diode.data._
import japgolly.scalajs.react._
import utils.RxObserver

import scala.concurrent.Future

trait ReactCollection[T] extends ReactModel {

  import rx._

  val name: String
  protected val model: Var[Pot[Seq[T]]] = Var(Empty)
  private var runningCallback: Option[Future[Seq[T]]] = None

  def loadModel: Future[Seq[T]]

  private def load = {
    model() = readObs.now.pending()

    println(s"Loading $name")
    runningCallback = Some(loadModel.map { e =>
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

  // observable access
  val readObs: Rx[Pot[Seq[T]]] = Rx.unsafe {
    model()
  }

  def render(f: Seq[T] => ReactElement) = ReadyComponent.component(ReadyComponent.Props(f))

  def renderPending(f: Long => ReactElement) = PendingComponent.component(PendingComponent.Props(f))

  def renderFailed(f: Throwable => ReactElement) = FailedComponent.component(FailedComponent.Props(f))

  object ReadyComponent {

    case class Props(render: Seq[T] => ReactElement)

    class Backend($: BackendScope[Props, Unit]) extends RxObserver($) {
      def mounted(p: Props) = observe(model)

      def render(p: Props) = readObs.now match {
        case Ready(content) => p.render(content)
        case _ => null
      }
    }

    val component = ReactComponentB[Props](s"$name-ReadyWrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .build
  }

  object PendingComponent {

    case class Props(render: Long => ReactElement)

    class Backend($: BackendScope[Props, Unit]) extends RxObserver($) {
      def mounted(p: Props) = observe(model)

      def render(p: Props) = readObs.now match {
        case Pending(startTime) => p.render(startTime)
        case _ => null
      }
    }

    val component = ReactComponentB[Props](s"$name-PendingWrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .build
  }

  object FailedComponent {

    case class Props(render: Throwable => ReactElement)

    class Backend($: BackendScope[Props, Unit]) extends RxObserver($) {
      def mounted(p: Props) = observe(model)

      def render(p: Props) = readObs.now match {
        case Failed(e) => p.render(e)
        case _ => null
      }
    }

    val component = ReactComponentB[Props](s"$name-FailedWrapper")
      .renderBackend[Backend]
      .componentDidMount($ => $.backend.mounted($.props))
      .build
  }

}