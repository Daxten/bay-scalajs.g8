package shared.utils

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import org.threeten.bp.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime}

/**
  * Created by alexe on 18.01.2017.
  */
trait Codecs {

  def createSimpleCodec[T](encode: T => String, decode: String => T): (Encoder[T], Decoder[T]) =
    (Encoder.encodeString.contramap[T](encode),
      Decoder.decodeString.emap(str => Either.catchNonFatal(decode(str)).leftMap(_.getMessage)))

  implicit val (encodeOffsetDateTime, decodeOffsetDateTime) = createSimpleCodec[OffsetDateTime](_.toString(), OffsetDateTime.parse)
  implicit val (encodeLocalDateTime, decodeLocalDateTime) = createSimpleCodec[LocalDateTime](_.toString(), LocalDateTime.parse)
  implicit val (encodeLocalDate, decodeLocalDate) = createSimpleCodec[LocalDate](_.toString(), LocalDate.parse)
  implicit val (encodeLocalTime, decodeLocalTime) = createSimpleCodec[LocalTime](_.toString(), LocalTime.parse)
}

object Codecs extends Codecs
