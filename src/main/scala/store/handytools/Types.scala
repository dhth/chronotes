package store.handytools

import scala.scalajs.js.Date

val minute = 60 * 1000

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
    theme: Theme,
    orderingChanged: Boolean
)

final case class PotentialNote(
    body: String,
    index: Option[Int] = None
)

final case class Note(id: Int, body: String, timestamp: Date)

def sampleNotes(baseLineDate: Date): Vector[Note] =
  val startDate = new Date(baseLineDate.getTime - minute * 201)
  Vector(
    ("Logged into backup AWS account", 0),
    ("Started copy of recovery point to source account", 5),
    ("Why is this taking so long?...", 52),
    ("Gotta be done soon...", 88),
    ("Surely, anytime now...", 152),
    ("Realized I chose the wrong recovery point", 200),
    ("Shut down computer", 201)
  ).map((body, i) => Note(i, body, new Date(startDate.getTime + minute * i)))

def getNoteLine(note: Note): String =
  s"${note.timestamp.toLocaleString}: ${note.body}"

enum TimestampUpdateType:
  case PushBehind, PushForward

object Model {
  def init(): Model =
    Model(
      currentNote = None,
      notes = Vector.empty,
      recentlyCopied = false,
      theme = Theme.Dark,
      orderingChanged = false
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
  case UserRequestedSampleNotes
  case CurrentTimeFetchedForSampleNotes(date: Date)
  case UserRequestedReset
  case NoteOrderingChanged
  case ResetNoteOrderingFlash
  case NoOp
