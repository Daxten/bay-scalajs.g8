package shared.models

import io.circe.{Decoder, Encoder, HCursor, Json}
import org.threeten.bp.{Instant, OffsetDateTime, ZoneOffset}
import cats.syntax.either._

object SharedDefault extends shared.models.auto_generated.SharedDefault {

  // You can do changes in this file, even though it will get autocreated if it's missing, it won't be overwritten

  case class BaseUser(id: Int, name: String)

  implicit val encodeOffsetDateTime: Encoder[OffsetDateTime] = Encoder.encodeString.contramap[OffsetDateTime](t => s"${t.toEpochSecond} ${t.getOffset.getTotalSeconds}")

  implicit val decodeOffsetDateTime: Decoder[OffsetDateTime] = Decoder.decodeString.emap { str =>
    val Array(i, s) = str.split(" ")
    val zoneOffset  = ZoneOffset.ofTotalSeconds(s.toInt)
    val instant     = Instant.ofEpochSecond(i.toLong)
    val offsetDateTime = OffsetDateTime.ofInstant(instant, zoneOffset)

    Either.catchNonFatal(offsetDateTime).leftMap(t => "OffsetDateTime")
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
