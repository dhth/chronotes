package store.handytools

import scala.scalajs.js.Date

val minute = 60 * 1000

enum ManualTheme:
  case Dark
  case Light

  def name: String =
    this match
      case Dark  => "dark"
      case Light => "light"

  def icon: String =
    this match
      case Dark  => "🌙"
      case Light => "☀️"

enum Theme:
  case Manual(variant: ManualTheme)
  case System(dark: Option[Boolean])

  def name: String =
    this match
      case Manual(variant) => variant.name
      case System(_)       => "system"

  def icon: String =
    this match
      case Manual(variant) => variant.icon
      case System(_)       => "🔁"

  def next: Theme =
    this match
      case Manual(variant) =>
        variant match
          case ManualTheme.Dark  => Manual(variant = ManualTheme.Light)
          case ManualTheme.Light => System(dark = None)

      case System(_) => Manual(variant = ManualTheme.Dark)

final case class Model(
    currentNote: Option[PotentialNote],
    notes: Vector[Note],
    recentlyCopied: Boolean,
    theme: Theme,
    movedNoteId: Option[Int],
    numMovesInProgress: Int
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
      theme = Theme.System(dark = None),
      movedNoteId = None,
      numMovesInProgress = 0
    )
}

enum Msg:
  case PreviousThemeLoaded(theme: Theme)
  case SystemThemeFetched(dark: Boolean)
  case UserRequestedThemeChange
  case UserEnteredNoteBody(note: String)
  case UserSubmittedNewNote
  case NotePrepared(note: Note)
  case UserRequestedNoteDeletion(index: Int)
  case UserRequestedTimeStampBeUpdated(
      index: Int,
      changeType: TimestampUpdateType
  )
  case UserRequestedToEditNote(index: Int)
  case UserRequestedEditCancellation
  case UserRequestedCopyToClipboard
  case CopyContentsAttempted(errored: Boolean)
  case ResetCopyButton
  case UserRequestedSampleNotes
  case CurrentTimeFetchedForSampleNotes(date: Date)
  case UserRequestedReset
  case ResetNoteOrderingFlash
  case NoOp
