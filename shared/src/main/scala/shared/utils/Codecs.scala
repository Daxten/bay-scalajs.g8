package shared.utils

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import java.time._

trait Codecs extends UpickleCodecs with CirceCodecs

object Codecs extends Codecs

trait CirceCodecs {

  def createCirceCodec[T](encode: T => String, decode: String => T): (Encoder[T], Decoder[T]) =
    (Encoder.encodeString.contramap[T](encode), Decoder.decodeString.emap(str => Either.catchNonFatal(decode(str)).leftMap(_.getMessage)))

  implicit val (encodeOffsetDateTimeCirce, decodeOffsetDateTimeCirce) =
    createCirceCodec[OffsetDateTime](_.toString(), OffsetDateTime.parse)
  implicit val (encodeLocalDateTimeCirce, decodeLocalDateTimeCirce) =
    createCirceCodec[LocalDateTime](_.toString(), LocalDateTime.parse)
  implicit val (encodeLocalDateCirce, decodeLocalDateCirce) =
    createCirceCodec[LocalDate](_.toString(), LocalDate.parse)
  implicit val (encodeLocalTimeCirce, decodeLocalTimeCirce) =
    createCirceCodec[LocalTime](_.toString(), LocalTime.parse)
  implicit val (encodeDurationCirce, decodeDurationCirce) =
    createCirceCodec[Duration](_.toString(), Duration.parse)

}

object CirceCodecs extends CirceCodecs

trait UpickleCodecs {
  import upickle.default._

  def createUpickleCode[T](encode: T => String, decode: String => T): (Writer[T], Reader[T]) =
    (Writer[T](e => upickle.Js.Str(encode(e))), Reader[T] {
      case upickle.Js.Str(jsStr) => decode(jsStr)
    })

  implicit val (encodeOffsetDateTimeUpickle, decodeOffsetDateTimeUpickle) =
    createUpickleCode[OffsetDateTime](_.toString(), OffsetDateTime.parse)
  implicit val (encodeLocalDateTimeUpickle, decodeLocalDateTimeUpickle) =
    createUpickleCode[LocalDateTime](_.toString(), LocalDateTime.parse)
  implicit val (encodeLocalDateUpickle, decodeLocalDateUpickle) =
    createUpickleCode[LocalDate](_.toString(), LocalDate.parse)
  implicit val (encodeLocalTimeUpickle, decodeLocalTimeUpickle) =
    createUpickleCode[LocalTime](_.toString(), LocalTime.parse)
  implicit val (encodeDurationUpickle, decodeDurationUpickle) =
    createUpickleCode[Duration](_.toString(), Duration.parse)
}
