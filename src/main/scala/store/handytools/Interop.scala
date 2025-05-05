package store.handytools

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("navigator.clipboard")
object Clipboard extends js.Object {
  // https://developer.mozilla.org/en-US/docs/Web/API/Clipboard
  def writeText(text: String): js.Promise[Unit] = js.native
}
