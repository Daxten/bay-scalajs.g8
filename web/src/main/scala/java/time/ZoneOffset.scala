package java.time

/**
  * Created by Haak on 02.05.2016.
  */
object ZoneOffset {
  def ofTotalSeconds(totalSeconds: Int): ZoneOffset = new ZoneOffset(totalSeconds)
}

final class ZoneOffset(val totalSeconds: Int) {
  def getTotalSeconds: Int = totalSeconds
}
