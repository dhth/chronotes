package store.handytools

import tyrian.Cmd
import cats.effect.IO
import scala.scalajs.js.Date
import scala.concurrent.duration.DurationInt

object Update {
  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.PreviousThemeLoaded(theme) => (model.copy(theme = theme), Cmd.None)

    case Msg.SystemThemeFetched(isSystemThemeDark) =>
      (model.copy(theme = Theme.System(Some(isSystemThemeDark))), Cmd.None)

    case Msg.PreviousNotesFetched(notesFromStorage) =>
      if (notesFromStorage.isEmpty) {
        (model, Cmd.None)
      } else {
        (
          model.copy(notes = (notesFromStorage ++ model.notes).sortByTimestamp),
          Cmd.None
        )
      }

    case Msg.UserRequestedThemeChange =>
      val nextTheme = model.theme.next

      val effect = nextTheme match {
        case Theme.Manual(variant) => Effects.setManualTheme(variant)
        case Theme.System(_)       => Effects.setSystemTheme
      }

      (model.copy(theme = nextTheme), effect)

    case Msg.UserEnteredNoteBody(body) =>
      val current_note = model.currentNote match
        case None                => PotentialNote(body = body.trim)
        case Some(existing_note) => existing_note.copy(body = body.trim)
      (model.copy(currentNote = Some(current_note)), Cmd.None)

    case Msg.UserSubmittedNote =>
      model.currentNote match
        case None       => (model, Cmd.None)
        case Some(note) =>
          note.index match
            case None =>
              (
                model.copy(currentNote = None),
                Effects.getNoteWithCurrentTime(model.notes.length, note.body)
              )
            case Some(index) =>
              model.notes
                .lift(index)
                .fold((model.copy(currentNote = None), Cmd.None)) {
                  existing_note =>
                    val updatedNote  = existing_note.copy(body = note.body)
                    val updatedNotes = model.notes.updated(index, updatedNote)

                    (
                      model.copy(
                        currentNote = None,
                        notes = updatedNotes
                      ),
                      Effects.saveNotesToStorage(updatedNotes)
                    )
                }

    case Msg.NoOp => (model, Cmd.None)

    case Msg.NotePrepared(note) =>
      val notes = model.notes :+ note
      (model.copy(notes = notes), Effects.saveNotesToStorage(notes))

    case Msg.UserRequestedNoteDeletion(index) =>
      if (index >= model.notes.length) {
        (model, Cmd.None)
      } else {
        val notes = model.notes.patch(index, Nil, 1)
        (
          model.copy(notes = notes),
          Effects.saveNotesToStorage(notes)
        )
      }

    case Msg.UserRequestedTimeStampBeUpdated(index, changeType) =>
      model.notes.lift(index).fold((model, Cmd.None)) { note =>
        val delta = changeType match
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
        val updatedNotes = model.notes
          .updated(index, updatedNote)
          .sortByTimestamp

        if (updatedNotes.map(_.id) == model.notes.map(_.id)) {
          (
            model.copy(notes = updatedNotes),
            Effects.saveNotesToStorage(updatedNotes)
          )
        } else {
          (
            model.copy(
              notes = updatedNotes,
              movedNoteId = Some(note.id),
              numMovesInProgress = model.numMovesInProgress + 1
            ),
            Cmd.Batch(
              Cmd.emitAfterDelay(Msg.ResetNoteOrderingFlash, 1.second),
              Effects.saveNotesToStorage(updatedNotes)
            )
          )
        }
      }

    case Msg.UserRequestedToEditNote(index) =>
      model.notes.lift(index).fold((model, Cmd.None)) { note =>
        (
          model.copy(
            currentNote =
              Some(PotentialNote(body = note.body, index = Some(index)))
          ),
          Effects.focusElementById("note-input")
        )
      }

    case Msg.UserRequestedEditCancellation =>
      (model.copy(currentNote = None), Cmd.None)

    case Msg.UserRequestedCopyToClipboard =>
      val notes = model.notes.map(getNoteLine).mkString("\n")
      (model, Effects.copyToClipboard(notes))

    case Msg.CopyContentsAttempted(errored) =>
      errored match
        case true  => (model, Cmd.None)
        case false =>
          (
            model.copy(recentlyCopied = true),
            Cmd.emitAfterDelay(Msg.ResetCopyButton, 1.second)
          )

    case Msg.ResetCopyButton => (model.copy(recentlyCopied = false), Cmd.None)

    case Msg.UserRequestedSampleNotes =>
      (model, Effects.getCurrentDate)

    case Msg.CurrentTimeFetchedForSampleNotes(date) =>
      val notes = sampleNotes(date)
      (model.copy(notes = notes), Effects.saveNotesToStorage(notes))

    case Msg.UserRequestedReset =>
      (
        model.copy(
          currentNote = None,
          notes = Vector.empty,
          recentlyCopied = false
        ),
        Cmd.Batch(
          Effects.focusElementById("note-input"),
          Effects.removeNotesFromStorage
        )
      )

    case Msg.ResetNoteOrderingFlash =>
      (
        model.numMovesInProgress match {
          case n if n <= 1 =>
            model.copy(movedNoteId = None, numMovesInProgress = 0)
          case n => model.copy(numMovesInProgress = n - 1)
        },
        Cmd.None
      )
}
