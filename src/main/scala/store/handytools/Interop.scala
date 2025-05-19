package store.handytools

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import org.scalajs.dom.{document, window}
import ManualTheme.*

@js.native
@JSGlobal("navigator.clipboard")
object Clipboard extends js.Object {
  // https://developer.mozilla.org/en-US/docs/Web/API/Clipboard
  def writeText(text: String): js.Promise[Unit] = js.native
}

object ThemeOps {
  def setManualTheme(theme: ManualTheme): Unit = {
    window.localStorage.setItem("theme", theme.name)
    document.documentElement.setAttribute("data-theme", theme.name)
  }

  def setSystemTheme: Boolean = {
    window.localStorage.removeItem("theme")
    val isSystemThemeDark = window
      .matchMedia("(prefers-color-scheme: dark)")
      .matches
    isSystemThemeDark match {
      case true =>
        setTheme(Dark)

      case false =>
        setTheme(Light)
    }
    isSystemThemeDark
  }

  def getAndApplyPreviousTheme: Theme = {
    val storedTheme = Option(window.localStorage.getItem("theme"))

    storedTheme match {
      case Some("dark") =>
        setTheme(Dark)
        Theme.Manual(variant = Dark)

      case Some("light") =>
        setTheme(Light)
        Theme.Manual(variant = Light)

      case Some(_) =>
        setTheme(Dark)
        Theme.System(dark = Some(true))

      case None =>
        val isSystemThemeDark = window
          .matchMedia("(prefers-color-scheme: dark)")
          .matches
        isSystemThemeDark match {
          case true =>
            setTheme(Dark)

          case false =>
            setTheme(Light)
        }
        Theme.System(dark = Some(isSystemThemeDark))
    }
  }

  def setTheme(theme: ManualTheme): Unit =
    document.documentElement.setAttribute(
      "data-theme",
      theme.name
    )
}
