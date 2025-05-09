package store.handytools

import tyrian.Cmd
import cats.effect.IO
import Extensions.*
import scala.scalajs.js.Date
import Effects.{copyToClipboard, focusElementById, getCurrentDate}
import scala.concurrent.duration.DurationInt

object Update {
  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.UserThemeLoaded(theme) => (model.copy(theme = theme), Cmd.None)

    case Msg.UserEnteredNoteBody(body) =>
      val current_note = model.currentNote match
        case None                => PotentialNote(body = body.trim)
        case Some(existing_note) => existing_note.copy(body = body.trim)
      (model.copy(currentNote = Some(current_note)), Cmd.None)

    case Msg.UserSubmittedNewNote =>
      model.currentNote match
        case None => (model, Cmd.None)
        case Some(note) =>
          note.index match
            case None =>
              (
                model.copy(currentNote = None),
                note.body |> Effects.getNoteWithCurrentTime
              )
            case Some(index) =>
              model.notes
                .lift(index)
                .fold((model.copy(currentNote = None), Cmd.None)) {
                  existing_note =>
                    val updatedNote = existing_note.copy(body = note.body)

                    (
                      model.copy(
                        currentNote = None,
                        notes = model.notes.updated(index, updatedNote)
                      ),
                      Cmd.None
                    )
                }

    case Msg.NoOp => (model, Cmd.None)

    case Msg.NotePrepared(note) =>
      (model.copy(notes = model.notes :+ note), Cmd.None)

    case Msg.UserRequestedNoteDeletion(index) =>
      if (index >= model.notes.length) {
        (model, Cmd.None)
      } else {
        (
          model.copy(notes = model.notes.patch(index, Nil, 1)),
          Cmd.None
        )
      }

    case Msg.UserRequestedTimeStampBeUpdated(index, change_type) =>
      model.notes.lift(index).fold((model, Cmd.None)) { note =>
        val delta = change_type match
          case TimestampUpdateType.PushBehind  => -1 * 60 * 1000
          case TimestampUpdateType.PushForward => 60 * 1000

        val updatedNote =
          note
            .copy(timestamp =
              new Date(
                note.timestamp.getTime()
                  + delta
              )
            )

        (model.copy(notes = model.notes.updated(index, updatedNote)), Cmd.None)
      }

    case Msg.UserRequestedToEditNote(index) =>
      model.notes.lift(index).fold((model, Cmd.None)) { note =>
        (
          model.copy(
            currentNote =
              Some(PotentialNote(body = note.body, index = Some(index)))
          ),
          "note-input" |> focusElementById
        )
      }

    case Msg.UserRequestedEditCancellation =>
      (model.copy(currentNote = None), Cmd.None)

    case Msg.UserRequestedCopyToClipboard =>
      val notes = model.notes.map(getNoteLine).mkString("\n")
      (model, notes |> copyToClipboard)

    case Msg.CopyContentsAttempted(errored) =>
      errored match
        case true => (model, Cmd.None)
        case false =>
          (
            model.copy(recentlyCopied = true),
            Cmd.emitAfterDelay(Msg.ResetCopyButton, 1.second)
          )

    case Msg.ResetCopyButton => (model.copy(recentlyCopied = false), Cmd.None)
    case Msg.UserThemeChanged =>
      (model.copy(theme = model.theme |> Theme.getNext), Cmd.None)
    case Msg.UserRequestedSampleNotes =>
      (model, getCurrentDate())
    case Msg.CurrentTimeFetchedForSampleNotes(date) =>
      (model.copy(notes = date |> sampleNotes), Cmd.None)
    case Msg.UserRequestedReset =>
      (
        model.copy(
          currentNote = None,
          notes = Vector.empty,
          recentlyCopied = false
        ),
        "note-input" |> focusElementById
      )
}
