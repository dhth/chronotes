package store.handytools

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import org.scalajs.dom.{document, window}
import io.circe.generic.auto.*
import io.circe.parser.decode
import io.circe.syntax.*
import ManualTheme.*
import DateCodecs.given

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

object Storage {
  def getNotes: Vector[Note] =
    Option(window.localStorage.getItem("notes")) match {
      case None           => Vector.empty
      case Some(notesStr) =>
        decode[Vector[Note]](notesStr) match {
          case Left(errors) =>
            window.localStorage.removeItem("notes")
            Vector.empty
          case Right(notes) => notes
        }
    }

  def storeNotes(notes: Vector[Note]): Unit =
    val notesString = notes.asJson.toString
    window.localStorage.setItem("notes", notesString)

  def removeNotes: Unit =
    window.localStorage.removeItem("notes")
}
