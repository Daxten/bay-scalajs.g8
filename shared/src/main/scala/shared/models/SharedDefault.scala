package shared.models

import shared.utils.Codecs

object SharedDefault extends shared.models.auto_generated.SharedDefault with Codecs {

  // You can do changes in this file, even though it will get autocreated if it's missing, it won't be overwritten

  case class BaseUser(id: Int, name: String)

  case class SearchResult[T](list: Map[Int, T], startOffset: Int, nextOffset: Int, count: Int)

  case class MutableForm[T](orig: T, state: T, loading: Boolean, error: Boolean) {
    def hasChanges     = orig != state
    def withError      = copy(error = true)
    def startLoading   = copy(loading = true)
    def update(s: T)   = copy(state = s)
    def mod(f: T => T) = copy(state = f(state))
    def stateUpdated   = copy(orig = state)
  }

  object MutableForm {
    def create[T](e: T) = MutableForm(e, e, false, false)
  }
}
