package store.handytools

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import org.scalajs.dom.{document, window}

@js.native
@JSGlobal("navigator.clipboard")
object Clipboard extends js.Object {
  // https://developer.mozilla.org/en-US/docs/Web/API/Clipboard
  def writeText(text: String): js.Promise[Unit] = js.native
}

object ThemeOps {
  def setManualTheme(theme: Theme): Unit = {
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
        setTheme(Theme.Dark)

      case false =>
        setTheme(Theme.Light)
    }
    isSystemThemeDark
  }

  def getAndApplyPreviousTheme: Theme = {
    val storedTheme = Option(window.localStorage.getItem("theme"))

    storedTheme match {
      case Some("dark") =>
        setTheme(Theme.Dark)
        Theme.Dark

      case Some("light") =>
        setTheme(Theme.Light)
        Theme.Light

      case Some(_) =>
        setTheme(Theme.Dark)
        Theme.System(dark = Some(true))

      case None =>
        val isSystemThemeDark = window
          .matchMedia("(prefers-color-scheme: dark)")
          .matches
        isSystemThemeDark match {
          case true =>
            setTheme(Theme.Dark)

          case false =>
            setTheme(Theme.Light)
        }
        Theme.System(dark = Some(isSystemThemeDark))
    }
  }

  def setTheme(theme: Theme): Unit =
    document.documentElement.setAttribute(
      "data-theme",
      theme.name
    )

  def trackSystemTheme(cb: (Boolean) => Unit): Unit =
    window
      .matchMedia("(prefers-color-scheme: dark)")
      .addEventListener(
        "change",
        _ =>
          if (Option(window.localStorage.getItem("theme")).isEmpty) { // this ensures we only change the theme when "auto" mode is ON
            val isSystemThemeDark = window
              .matchMedia("(prefers-color-scheme: dark)")
              .matches

            if (isSystemThemeDark) {
              setTheme(Theme.Dark)
            } else {
              setTheme(Theme.Light)
            }
            cb(isSystemThemeDark)

          }
      )
}
