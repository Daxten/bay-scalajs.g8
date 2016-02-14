package stores.engine

import diode.data.Ready
import japgolly.scalajs.react.Callback

import scala.concurrent.Future

/**
  * Created by Alexej on 28.01.2016.
  */
trait UpdateableReactCollection[T] extends ReactCollection[T] {

  def equals(a: T, b: T): Boolean

  def update(changed: T): Future[Seq[T]] = {
    for {
      data <- smartLoad
    } yield {
      val updatedModel = {
        if (data.exists(equals(_, changed))) data.map(x => if (equals(changed, x)) changed else x)
        else data :+ changed
      }
      model() = Ready(updatedModel)
      updatedModel
    }
  }

  def update(changes: Seq[T]): Future[Seq[T]] = {
    for {
      data <- smartLoad
    } yield {
      val updatedModel = changes.foldLeft(data)((coll, e) => {
        coll.find(equals(e, _)).fold {
          coll :+ e
        } { _ =>
          coll.map(x => if (equals(e, x)) e else x)
        }
      })
      model() = Ready(updatedModel)
      updatedModel
    }
  }
}
