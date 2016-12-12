import scalaz._
import Scalaz._

case class Prof(name: String, year: Int)

val x = Free.point("Hallo")

x.