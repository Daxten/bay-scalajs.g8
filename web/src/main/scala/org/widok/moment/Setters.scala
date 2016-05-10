package org.widok.moment

import scala.scalajs.js

@js.native
trait Setters[T] extends js.Object {
  def add(time: Int, unit: String): T = js.native
  def add(time: Double, unit: String): T = js.native
  def add(millis: Int): T = js.native
  def add(duration: Duration): T = js.native

  def subtract(time: Int, unit: String): T = js.native
  def subtract(time: Double, unit: String): T = js.native
  def subtract(millis: Int): T = js.native
  def subtract(duration: Duration): T = js.native
}
