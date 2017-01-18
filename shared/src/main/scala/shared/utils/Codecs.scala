package shared.utils

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import org.threeten.bp.OffsetDateTime

/**
  * Created by alexe on 18.01.2017.
  */
trait Codecs {
  implicit val encodeOffsetDateTime: Encoder[OffsetDateTime] = Encoder.encodeString.contramap[OffsetDateTime](_.toString())

  implicit val decodeOffsetDateTime: Decoder[OffsetDateTime] = Decoder.decodeString.emap { str =>
    val offsetDateTime = OffsetDateTime.parse(str)

    Either.catchNonFatal(offsetDateTime).leftMap(_.getMessage)
  }
}

object Codecs extends Codecs
