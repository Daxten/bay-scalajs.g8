package shared.models

import shared.utils.Codecs

object SharedDefault extends shared.models.auto_generated.SharedDefault with Codecs {
  case class MutableForm[T](orig: T, state: T, loading: Boolean, error: Boolean) {
    def hasChanges: Boolean            = orig != state
    def withError: MutableForm[T]      = copy(error = true)
    def startLoading: MutableForm[T]   = copy(loading = true)
    def update(s: T): MutableForm[T]   = copy(state = s)
    def mod(f: T => T): MutableForm[T] = copy(state = f(state))
    def stateUpdated: MutableForm[T]   = copy(orig = state)
  }

  object MutableForm {
    def create[T](e: T) = MutableForm(e, e, loading = false, error = false)
  }
}
