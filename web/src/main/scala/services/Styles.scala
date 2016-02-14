package services

import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry

object Styles extends StyleSheet.Inline {

  import dsl._

  val bayRed = rgba(236, 34, 39, 1)

  def loadGlobal = {
    GlobalRegistry.register(
    )

    GlobalRegistry.addToDocumentOnRegistration()
  }
}
