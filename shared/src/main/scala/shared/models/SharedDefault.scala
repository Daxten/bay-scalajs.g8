
package shared.models

import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

import upickle.Js

object SharedDefault extends shared.models.auto_generated.SharedDefault {

  // You can do changes in this file, even though it will get autocreated if it's missing, it won't be overwritten

  case class BaseUser(id: Int, name: String)

  val x = OffsetDateTime.now.getDayOfMonth.toString
  val y = ZoneOffset.ofTotalSeconds(500).getTotalSeconds.toString

  implicit val offsetDateTime2Writer = upickle.default.Writer[OffsetDateTime] {
    case t => Js.Str(s"${t.toEpochSecond} ${t.getOffset.getTotalSeconds}")
  }

  implicit val thing2Reader = upickle.default.Reader[OffsetDateTime] {
    case Js.Str(str) =>
      val Array(i, s) = str.split(" ")
      val zoneOffset = ZoneOffset.ofTotalSeconds(s.toInt)
      val localDateTime = LocalDateTime.ofEpochSecond(i.toLong, 0, zoneOffset)
      OffsetDateTime.of(localDateTime, zoneOffset)
  }

  case class SearchResult[T](list: Map[Int, T], startOffset: Int, nextOffset: Int, count: Int)

  case class MutableForm[T](orig: T, state: T, loading: Boolean, error: Boolean) {
    def hasChanges = orig != state
    def withError = copy(error = true)
    def startLoading = copy(loading = true)
    def update(s: T) = copy(state = s)
    def mod(f: T => T) = copy(state = f(state))
    def stateUpdated = copy(orig = state)
  }

  object MutableForm {
    def create[T](e: T) = MutableForm(e, e, false, false)
  }
}
     