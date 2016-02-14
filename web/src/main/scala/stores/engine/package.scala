package stores

package object engine {
  implicit val ec = scalajs.concurrent.JSExecutionContext.Implicits.queue
}
