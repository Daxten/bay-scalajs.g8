package org.widok.moment

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native
@JSName("moment")
object Moment extends js.Object {
  def apply(): Date = js.native

  /* Long has different semantics than JavaScript's numbers, therefore Double
   * must be used.
   */
  def apply(millis: Double): Date = js.native

  def apply(moment: Date): Date = js.native
  def apply(date: js.Date): Date = js.native
  def apply(string: String): Date = js.native
  def apply(string: String, format: String): Date = js.native
  def apply(string: String, format: String, locale: String): Date = js.native
  def apply(string: String, format: String, strict: Boolean): Date = js.native
  def apply(string: String, format: String, locale: String, strict: Boolean): Date = js.native

  def utc(): Date = js.native

  /* Long has different semantics than JavaScript's numbers, therefore Double
   * must be used.
   */
  def utc(millis: Double): Date = js.native

  def utc(arr: js.Array[Int]): Date = js.native
  def utc(string: String): Date = js.native
  def utc(string: String, format: String): Date = js.native
  def utc(string: String, formats: js.Array[String]): Date = js.native
  def utc(string: String, format: String, locale: String): Date = js.native
  def utc(moment: Date): Date = js.native
  def utc(date: js.Date): Date = js.native

  def locale(s: String): Unit = js.native

  def duration(millis: Int): Duration = js.native
  def duration(time: Int, unit: String): Duration = js.native
  def duration(time: String): Duration = js.native

  def weekdaysShort(): js.Array[String] = js.native
  def weekdaysShort(index: Int): String = js.native
}
