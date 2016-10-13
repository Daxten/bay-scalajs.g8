package shared.models

import org.threeten.bp.{Instant, LocalDateTime, OffsetDateTime, ZoneOffset}
import upickle.Js

object SharedDefault extends shared.models.auto_generated.SharedDefault {

  // You can do changes in this file, even though it will get autocreated if it's missing, it won't be overwritten

  case class BaseUser(id: Int, name: String)

  implicit val offsetDateTime2Writer = upickle.default.Writer[OffsetDateTime] {
    case t => Js.Str(s"${t.toEpochSecond} ${t.getOffset.getTotalSeconds}")
  }

  implicit val thing2Reader = upickle.default.Reader[OffsetDateTime] {
    case Js.Str(str) =>
      val Array(i, s) = str.split(" ")
      val zoneOffset  = ZoneOffset.ofTotalSeconds(s.toInt)
      val instant     = Instant.ofEpochSecond(i.toLong)
      OffsetDateTime.ofInstant(instant, zoneOffset)
  }

  case class SearchResult[T](list: Map[Int, T], startOffset: Int, nextOffset: Int, count: Int)

  case class MutableForm[T](orig: T, state: T, loading: Boolean, error: Boolean) {
    def hasChanges     = orig != state
    def withError      = copy(error = true)
    def startLoading   = copy(loading = true)
    def update(s: T)   = copy(state = s)
    def mod(f: T => T) = copy(state = f(state))
    def stateUpdated   = copy(orig = state)
  }

  object MutableForm {
    def create[T](e: T) = MutableForm(e, e, false, false)
  }
}
