package java.time

import org.widok.moment._

/**
  * Created by Haak on 02.05.2016.
  */
object LocalDateTime {
  def ofEpochSecond(epochSecond: Long, nanoSecond: Int, zoneOffset: ZoneOffset) = {
    val moment = Moment.utc(epochSecond * 1000 * 1000 + nanoSecond)
    moment.utcOffset(zoneOffset.getTotalSeconds / (60 * 60))
    new LocalDateTime(moment)
  }
}

final class LocalDateTime(val moment: Date) {
  def toEpochSecond(zoneOffset: ZoneOffset): Long = moment.format("X").toLong
}
