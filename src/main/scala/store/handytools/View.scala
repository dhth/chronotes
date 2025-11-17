package store.handytools

import tyrian.Html
import tyrian.Html.*
import tyrian.Empty

object View {
  def mainSection(model: Model): Html[Msg] =
    div(_class := "flex flex-col py-4 max-sm:min-h-screen sm:h-screen")(
      heading(model.theme),
      notesInput(model.currentNote),
      notesSection(model),
      allNotes(model.notes, model.recentlyCopied)
    )

  private def heading(theme: Theme): Html[Msg] =
    div(_class := "flex gap-2 items-center")(
      div(_class := "flex flex-1 gap-2 items-center")(
        h1(
          _class := "text-3xl font-semibold dark:text-blue-300 text-blue-800"
        )(
          "chronotes"
        ),
        p(_class := "text-sm font-semibold dark:text-slate-400 text-slate-700")(
          "(wip)"
        )
      ),
      button(
        _class := "text-2xl",
        title  := themeTooltip(theme),
        onClick(Msg.UserRequestedThemeChange)
      )(
        theme.icon
      )
    )

  private def themeTooltip(theme: Theme): String =
    s"using ${theme.name} theme; click to switch to ${theme.next.name}"

  private def notesInput(note: Option[PotentialNote]): Html[Msg] =
    val (verb, inputOutlineClass, buttonBgClass) = note.flatMap(_.index) match
      case None =>
        (
          "add",
          "dark:focus:outline-blue-400 focus:outline-blue-700",
          "dark:bg-blue-400 bg-blue-300"
        )
      case Some(_) =>
        (
          "update",
          "dark:focus:outline-orange-500 focus:outline-orange-400",
          "dark:bg-orange-500 bg-orange-400"
        )

    div(_class := "mt-4")(
      p(_class := "font-semibold")(
        s"Type note entry and press enter or click \"${verb}\""
      ),
      form(_class := "flex gap-2 max-sm:gap-1 items-center mt-2")(
        input(
          _class := s"flex-1 outline-2 ${inputOutlineClass} dark:outline-gray-400 outline-slate-500 h-10 max-sm:h-8 p-2",
          id           := "note-input",
          autoComplete := "off",
          attribute("data-1p-ignore", ""),
          value := note.map(_.body).getOrElse(""),
          onInput(Msg.UserEnteredNoteBody(_))
        ),
        button(
          _class :=
            s"${buttonBgClass} text-neutral-800 "
              ++ "dark:disabled:bg-neutral-400 disabled:bg-neutral-300 text-lg p-2 font-bold "
              ++ "cursor-pointer max-sm:text-sm",
          disabled(note.map(_.body.isEmpty).getOrElse(true)),
          onClick(Msg.UserSubmittedNote)
        )(
          verb
        ),
        note.map(_.body.isEmpty).getOrElse(true) match
          case true  => Empty
          case false =>
            button(
              _class := List(
                "dark:bg-rose-500 bg-rose-400 text-neutral-800",
                "text-lg p-2 font-bold cursor-pointer max-sm:text-sm"
              ).mkString(" "),
              onClick(Msg.UserRequestedEditCancellation)
            )("cancel")
      )
    )

  private def notesSection(model: Model): Html[Msg] =
    if (model.notes.isEmpty) {
      introSection()
    } else {
      val inputs = model.notes.zipWithIndex.map((note, index) =>
        noteEntry(
          note,
          index,
          model.currentNote.flatMap(_.index),
          model.movedNoteId.map(i => note.id == i).getOrElse(false)
        )
      )

      div(
        _class := "flex-1 mt-4 border-2 border-dotted dark:border-blue-400 border-blue-600 "
          ++ "border-opacity-10 p-4 md:overflow-y-auto dark:bg-slate-800 bg-blue-100"
      )(
        div(_class := "flex gap-2 items-center")(
          p(_class := "flex-1 text-xl")("Entries"),
          button(
            _class := s"px-2 py-1 right-0 dark:bg-red-500 bg-red-400 text-neutral-800 font-bold cursor-pointer",
            title := "reset",
            onClick(Msg.UserRequestedReset)
          )("reset")
        ),
        div(_class := "mt-4 md:flex-1 flex flex-col gap-4")(
          inputs*
        )
      )
    }

  private def introSection(): Html[Msg] =
    div(
      _class := "flex flex-col gap-4 mt-4 border-2 border-dotted dark:border-blue-400 "
        ++ "border-blue-700 p-8 max-sm:p-4 overflow-x-auto md:overflow-y-auto "
        ++ "dark:bg-slate-800 bg-blue-100"
    )(
      pre(
        _class := "mx-auto hidden sm:block dark:text-blue-400 text-blue-800 font-semibold mb-4",
        id := "intro-banner"
      )("""
      __                                   __                    
     /\ \                                 /\ \__                 
  ___\ \ \___   _ __   ___     ___     ___\ \ ,_\    __    ____  
 /'___\ \  _ `\/\`'__\/ __`\ /' _ `\  / __`\ \ \/  /'__`\ /',__\ 
/\ \__/\ \ \ \ \ \ \//\ \L\ \/\ \/\ \/\ \L\ \ \ \_/\  __//\__, `\
\ \____\\ \_\ \_\ \_\\ \____/\ \_\ \_\ \____/\ \__\ \____\/\____/
 \/____/ \/_/\/_/\/_/ \/___/  \/_/\/_/\/___/  \/__/\/____/\/___/
"""),
      p(
        _class := "mx-auto text-xl font-semibold dark:text-cyan-300 text-cyan-800"
      )(
        "What is chronotes for?"
      ),
      p(_class := "mx-auto dark:text-amber-400 text-rose-900")(
        "Say you're following a checklist — such as for a software migration — and need to record each step along with a timestamp."
      ),
      p(_class := "mx-auto dark:text-orange-500 text-rose-700")(
        "You could use chronotes to handle the timestamps automatically and simply copy the final list."
      ),
      p(_class := "mx-auto dark:text-gray-400 text-gray-800 italic text-sm")(
        "(yes, it's for a very small niche :D)"
      ),
      button(
        _class := "dark:bg-cyan-400 bg-blue-300 text-neutral-800 mx-auto mt-4 px-4 py-1 font-semibold cursor-pointer",
        onClick(Msg.UserRequestedSampleNotes)
      )(
        "show me some samples"
      )
    )

  private def noteEntry(
      note: Note,
      index: Int,
      currentlyEditedIndex: Option[Int],
      recentlyMoved: Boolean
  ): Html[Msg] =
    val isDisabled = currentlyEditedIndex.isDefined
    val cursor     = if (isDisabled) {
      "cursor-not-allowed"
    } else {
      "cursor-pointer"
    }

    val movedClass = if (recentlyMoved) {
      "dark:text-orange-300 text-red-700 font-bold"
    } else {
      "font-semibold"
    }

    div(
      _class := "flex flex-col gap-2 items-left"
    )(
      p(
        _class := s"flex-1 ${movedClass}"
      )(
        s"${note.timestamp.toLocaleTimeString}: ${note.body}"
      ),
      div(_class := "flex gap-1 text-xs max-sm:text-sm")(
        button(
          _class := s"px-2 py-1 dark:bg-lime-400 bg-lime-400 disabled:bg-gray-400 text-neutral-800 font-bold ${cursor} ${isDisabled}",
          title := "edit note",
          disabled(isDisabled),
          onClick(
            Msg.UserRequestedToEditNote(index)
          )
        )("~"),
        button(
          _class := s"px-2 py-1 dark:bg-teal-400 bg-teal-300 disabled:bg-gray-400 text-neutral-800 font-bold ${cursor} ",
          title := "move timestamp backwards by a minute",
          disabled(isDisabled),
          onClick(
            Msg.UserRequestedTimeStampBeUpdated(
              index,
              TimestampUpdateType.PushBehind
            )
          )
        )("<"),
        button(
          _class := s"px-2 py-1 dark:bg-teal-400 bg-teal-300 disabled:bg-gray-400 text-neutral-800 font-bold ${cursor}",
          title := "move timestamp forwards by a minute",
          disabled(isDisabled),
          onClick(
            Msg.UserRequestedTimeStampBeUpdated(
              index,
              TimestampUpdateType.PushForward
            )
          )
        )(">"),
        button(
          _class := s"px-2 py-1 dark:bg-rose-500 bg-rose-300 disabled:bg-gray-400 text-neutral-800 font-bold ${cursor}",
          title := "delete note",
          disabled(isDisabled),
          onClick(Msg.UserRequestedNoteDeletion(index))
        )("x")
      )
    )

  private def allNotes(
      notes: Vector[Note],
      recentlyCopied: Boolean
  ): Html[Msg] =
    val (buttonClass, buttonText) = recentlyCopied match {
      case true  => ("dark:bg-lime-300 bg-lime-400", "copied!")
      case false => ("dark:bg-sky-300 bg-sky-300", "copy")
    }

    if (notes.isEmpty) {
      div()
    } else {
      div(
        _class := "mt-4 md:h-1/3 md:overflow-y-auto p-4 border-2 border-dotted dark:border-sky-400 "
          ++ "border-sky-700 border-opacity-10 dark:bg-slate-900 bg-sky-100"
      )(
        div(_class := "flex gap-2 items-center")(
          p(_class := "flex-1 text-lg")("Notes"),
          button(
            _class := s"px-2 py-1 right-0 ${buttonClass} text-neutral-800 font-bold cursor-pointer",
            title := "copy notes to clipboard",
            onClick(Msg.UserRequestedCopyToClipboard)
          )(buttonText)
        ),
        pre(
          _class := "text-sm mt-4 overflow-x-auto",
          id     := "rendered-notes"
        )(
          notes.map(getNoteLine).mkString("\n")
        )
      )
    }
}
