import scala.util.Try

package object app {
  implicit class ExtParamater(e: io.swagger.models.parameters.Parameter) {
    def getType: Option[String] = Try(e.asInstanceOf[io.swagger.models.parameters.PathParameter].getType).toOption
  }

  implicit class ExtString(s: String) {
    def toCamelCase: String      = if (s.isEmpty) s else s.head.toLower + s.tail
    def toUpperCamelCase: String = if (s.isEmpty) s else s.head.toUpper + s.tail
  }
}
