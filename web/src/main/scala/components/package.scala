import scala.concurrent.ExecutionContextExecutor

package object components {
  implicit val ec: ExecutionContextExecutor = scalajs.concurrent.JSExecutionContext.Implicits.queue
}
