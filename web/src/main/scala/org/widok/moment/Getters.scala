package org.widok.moment

import scala.scalajs.js

@js.native
trait Getters extends js.Object {
  def millisecond(): Int = js.native
  def second(): Int = js.native
  def minute(): Int = js.native
  def hour(): Int = js.native
  def day(): Int = js.native
  def month(): Int = js.native
  def year(): Int = js.native
  def date(): Int = js.native

  def dayOfYear(): Int = js.native
  def weeks(): Int = js.native
  def isoWeeks(): Int = js.native
}
