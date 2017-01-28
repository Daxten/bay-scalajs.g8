import scala.concurrent.ExecutionContextExecutor

package object screens {
  implicit val ec: ExecutionContextExecutor = scalajs.concurrent.JSExecutionContext.Implicits.queue
}
