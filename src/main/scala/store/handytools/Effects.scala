package store.handytools

import tyrian.Cmd
import cats.effect.IO
import scala.scalajs.js.Date
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window
import ManualTheme.*

object Effects {
  def getNoteWithCurrentTime(index: Int, input: String): Cmd[IO, Msg] =
    val io = IO {
      new Date()
    }
    Cmd.Run(io)(dt => Msg.NotePrepared(Note(index, input, dt)))

  def copyToClipboard(text: String): Cmd[IO, Msg] =
    val io = IO.fromPromise {
      IO {
        Clipboard
          .writeText(text)
          .`then`[Msg](_ => Msg.CopyContentsAttempted(errored = false))
          .`catch`[Msg](_ => Msg.CopyContentsAttempted(errored = true))
      }
    }

    Cmd.Run(io)(identity)

  def focusElementById(elementId: String): Cmd[IO, Nothing] =
    Cmd.SideEffect(
      Option(document.getElementById(elementId).asInstanceOf[html.Element])
        .foreach(_.focus())
    )

  def getCurrentDate(): Cmd[IO, Msg] =
    val io = IO {
      new Date()
    }
    Cmd.Run(io)(Msg.CurrentTimeFetchedForSampleNotes(_))

  def setManualTheme(theme: ManualTheme): Cmd[IO, Nothing] =
    Cmd.SideEffect(
      ThemeOps.setManualTheme(theme)
    )

  def setSystemTheme: Cmd[IO, Msg] =
    val io = IO {
      ThemeOps.setSystemTheme
    }

    Cmd.Run(io)(Msg.SystemThemeFetched(_))

  def applyPreviousTheme: Cmd[IO, Msg] = {
    val io = IO {
      ThemeOps.getAndApplyPreviousTheme
    }

    Cmd.Run(io)(Msg.PreviousThemeLoaded(_))
  }

  def startSystemThemeTracking: Cmd[IO, Msg] =
    val io = IO {
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
                ThemeOps.setTheme(Dark)
              } else {
                ThemeOps.setTheme(Light)
              }
              Cmd.Emit(Msg.SystemThemeFetched(isSystemThemeDark))
            } else {
              Cmd.Emit(Msg.SystemThemeFetched(false))
            }
        )
    }
    Cmd.SideEffect(io)
}
