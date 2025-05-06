package store.handytools

import tyrian.Cmd
import cats.effect.IO
import scala.scalajs.js.Date
import scala.concurrent.duration.FiniteDuration
import org.scalajs.dom.{document, window}
import org.scalajs.dom.html

object Effects {
  def getNoteWithCurrentTime(input: String): Cmd[IO, Msg] =
    val datetime = IO {
      new Date()
    }
    Cmd.Run(datetime)(dt => Msg.NotePrepared(Note(input, dt)))

  def copyToClipboard(text: String): Cmd[IO, Msg] =
    val clipboardIO = IO.fromPromise {
      IO {
        Clipboard
          .writeText(text)
          .`then`[Msg](_ => Msg.CopyContentsAttempted(errored = false))
          .`catch`[Msg](err => Msg.CopyContentsAttempted(errored = true))
      }
    }

    Cmd.Run(clipboardIO)(identity)

  def tickAfterDelay(msg: Msg, delay: FiniteDuration): Cmd[IO, Msg] =
    val delayedIO = IO.sleep(delay).as(msg)
    Cmd.Run(delayedIO)(identity)

  def focusElementById(elementId: String): Cmd[IO, Nothing] =
    Cmd.SideEffect(
      Option(document.getElementById(elementId).asInstanceOf[html.Element])
        .foreach(_.focus())
    )

  def loadUserTheme(): Cmd[IO, Msg] =
    val themeIO = IO {
      val prefersDark =
        window.matchMedia("(prefers-color-scheme: dark)").matches

      if (prefersDark) Theme.Dark else Theme.Light
    }

    Cmd.Run(themeIO)(Msg.UserThemeLoaded(_))

  def getCurrentDate(): Cmd[IO, Msg] =
    val datetime = IO {
      new Date()
    }
    Cmd.Run(datetime)(Msg.CurrentTimeFetchedForSampleNotes(_))
}
