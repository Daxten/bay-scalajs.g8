package stores.engine

import diode.data.Pot
import rx.Rx

trait ReactModel {
  val name: String
  val readObs: Rx[Pot[_]]
}
