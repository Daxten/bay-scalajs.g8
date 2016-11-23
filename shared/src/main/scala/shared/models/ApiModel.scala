package shared.models

/**
  * Created by alexe on 15.11.2016.
  */
object ApiModel {
  sealed trait Ordering
  case object Asc extends Ordering
  case object Desc extends Ordering

  case class SortMethod[T](property: T, order: Ordering)
}
