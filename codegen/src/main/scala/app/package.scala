import io.swagger.models.ArrayModel
import io.swagger.models.ComposedModel
import io.swagger.models.RefModel

import scala.util.Try

package object app {
  implicit class ExtParamater(e: io.swagger.models.parameters.Parameter) {
    def getType: Option[String] = e match {
      case e: io.swagger.models.parameters.PathParameter => Some(e.getType)
      case e: io.swagger.models.parameters.BodyParameter =>
        e.getSchema match {
          case am: ArrayModel                  => None // TODO
          case e: ComposedModel                => None
          case ref: RefModel                   => Some(ref.getSimpleRef)
          case mi: io.swagger.models.ModelImpl => Some(mi.getType)
        }
      case _ => None
    }

    def getFormat: Option[String] = e match {
      case e: io.swagger.models.parameters.PathParameter => Some(e.getFormat)
      case e: io.swagger.models.parameters.BodyParameter =>
        e.getSchema match {
          case am: ArrayModel                  => None // TODO
          case e: ComposedModel                => None
          case mi: io.swagger.models.ModelImpl => Some(mi.getFormat)
        }
      case _ => None
    }
  }

  implicit class ExtString(s: String) {
    def toCamelCase: String = if (s.isEmpty) s else s.head.toLower + s.tail
    def toUpperCamelCase: String =
      if (s.isEmpty) s else s.head.toUpper + s.tail
  }
}
