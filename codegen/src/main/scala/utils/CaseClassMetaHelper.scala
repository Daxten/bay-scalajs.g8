package utils

import scala.meta._

object CaseClassMetaHelper {
  def updateOrInsert(source: Source, caseClass: Stat): Tree = {
    val modelName = caseClass.collect {
      case q"case class $tname (...$paramss)" =>
        tname.value
    }.head

    source.transform {
      case c @ q"case class $tname (...$paramss)" if tname.value == modelName =>
        println(s"- Updating CaseClass $modelName")
        caseClass
      case c @ q"case class $tname (...$paramss) { ..$body }" if tname.value == modelName =>
        println(s"- Updating CaseClass $modelName (preserving body)")
        caseClass.transform {
          case q"case class $tname (...$paramss)" =>
            q"case class $tname (...$paramss) { ..$body }"
        }
      case q"trait $tname { ..$body }" if source.collect {
        case q"case class $cname (...$paramss)" if cname.value == modelName             => 1
        case q"case class $cname (...$paramss) { ..$body }" if cname.value == modelName => 1
      }.isEmpty =>
        println(s"- Can't find $modelName, adding it to trait $tname")
        q"trait $tname { ..${body :+ caseClass} }"
    }
  }
}
