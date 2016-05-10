package java.time

import org.widok.moment._

/**
  * Created by Haak on 02.05.2016.
  */
object OffsetDateTime {
  def now(): OffsetDateTime = new OffsetDateTime(Moment())

  def of(localDateTime: LocalDateTime, zoneOffset: ZoneOffset) = {
    val moment: Date = Moment(localDateTime.toEpochSecond(zoneOffset))
    moment.utcOffset(zoneOffset.getTotalSeconds / (60 * 60))
    new OffsetDateTime(moment)
  }
}

final class OffsetDateTime(val moment: Date) {
  override def toString: String = moment.toString

  def getDayOfMonth: Int = moment.date()

  def getDayOfYear: Int = moment.dayOfYear()

  def getHour: Int = moment.hour()

  def getMinute: Int = moment.minute()

  def getYear: Int = moment.year()

  def getMonthValue: Int = moment.month()

  def isAfter(other: OffsetDateTime): Boolean = moment.isAfter(other.moment)

  def isBefore(other: OffsetDateTime): Boolean = moment.isBefore(other.moment)

  def minusDays(days: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(days, Units.Day)
    new OffsetDateTime(newMoment)
  }

  def minusMinutes(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(n, Units.Minute)
    new OffsetDateTime(newMoment)
  }

  def minusMonths(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(n, Units.Month)
    new OffsetDateTime(newMoment)
  }

  def minusNanos(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(n / 1000000, Units.Millisecond)
    new OffsetDateTime(newMoment)
  }

  def minusSeconds(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(n, Units.Second)
    new OffsetDateTime(newMoment)
  }

  def minusWeeks(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(n, Units.Week)
    new OffsetDateTime(newMoment)
  }

  def minusYears(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).subtract(n, Units.Year)
    new OffsetDateTime(newMoment)
  }

  def plusDays(days: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(days, Units.Day)
    new OffsetDateTime(newMoment)
  }

  def plusMinutes(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(n, Units.Minute)
    new OffsetDateTime(newMoment)
  }

  def plusMonths(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(n, Units.Month)
    new OffsetDateTime(newMoment)
  }

  def plusNanos(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(n / 1000000, Units.Millisecond)
    new OffsetDateTime(newMoment)
  }

  def plusSeconds(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(n, Units.Second)
    new OffsetDateTime(newMoment)
  }

  def plusWeeks(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(n, Units.Week)
    new OffsetDateTime(newMoment)
  }

  def plusYears(n: Long): OffsetDateTime = {
    val newMoment = Moment(toEpochSecond).add(n, Units.Year)
    new OffsetDateTime(newMoment)
  }

  def getOffset: ZoneOffset =
    ZoneOffset.ofTotalSeconds(moment.utcOffset() * 60 * 60)

  def toEpochSecond: Long = moment.format("X").toLong

  override def equals(obj: Any): Boolean = {
    obj match {
      case e: OffsetDateTime => toEpochSecond == e.toEpochSecond
      case _ => false
    }
  }
}
