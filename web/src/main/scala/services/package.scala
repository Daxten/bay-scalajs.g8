import scala.concurrent.ExecutionContextExecutor

package object services {
  implicit val ec: ExecutionContextExecutor = scalajs.concurrent.JSExecutionContext.Implicits.queue
}
