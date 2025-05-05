package store.handytools

import scala.scalajs.js.Date

enum Theme:
  case Light, Dark

object Theme {
  def display(theme: Theme): String =
    theme match
      case Light => "☀️"
      case Dark  => "🌙"

  def getNext(theme: Theme): Theme =
    theme match
      case Theme.Light => Theme.Dark
      case Theme.Dark  => Theme.Light
}

final case class Model(
    currentNote: Option[PotentialNote],
    notes: Vector[Note],
    recentlyCopied: Boolean,
    theme: Theme
)

final case class PotentialNote(
    body: String,
    index: Option[Int] = None
)

final case class Note(body: String, timestamp: Date)

def get_note_line(note: Note): String =
  s"${note.timestamp.toLocaleString}: ${note.body}"

enum TimestampUpdateType:
  case PushBehind, PushForward

object Model {
  def init(): Model =
    Model(
      currentNote = None,
      notes = Vector.empty,
      recentlyCopied = false,
      theme = Theme.Dark
    )
}

enum Msg:
  case UserThemeLoaded(theme: Theme)
  case UserThemeChanged
  case UserEnteredNoteBody(note: String)
  case UserSubmittedNewNote
  case NotePrepared(note: Note)
  case UserRequestedNoteDeletion(index: Int)
  case UserRequestedTimeStampBeUpdated(
      index: Int,
      change_type: TimestampUpdateType
  )
  case UserRequestedToEditNote(index: Int)
  case UserRequestedEditCancellation
  case UserRequestedCopyToClipboard
  case CopyContentsAttempted(errored: Boolean)
  case ResetCopyButton
  case NoOp
