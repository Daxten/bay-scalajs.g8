package object components {
  implicit val ec = scalajs.concurrent.JSExecutionContext.Implicits.queue
}
