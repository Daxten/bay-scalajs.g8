package utils

import japgolly.scalajs.react.vdom.{Attrs, Tags, Base}

trait ReactTags extends Base {
  @inline def < = Tags

  @inline def ^ = Attrs
}
