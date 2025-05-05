package store.handytools

import tyrian.Html
import tyrian.Html.*
import tyrian.Empty
import Extensions.*

object View {
  def main_section(model: Model): Html[Msg] =
    div(_class := "flex flex-col pt-4 pb-2 max-sm:min-h-screen md:h-screen")(
      heading(),
      context(),
      model.currentNote |> notes_input,
      model |> notes_section,
      all_notes(model.notes, model.recentlyCopied)
    )

  private def heading(): Html[Msg] =
    div(_class := "flex gap-4 items-center")(
      h1(_class := "flex-1 text-3xl font-semibold text-[#b8bb26]")(
        "chronotes"
      )
    )

  private def context(): Html[Msg] =
    div(_class := "mt-4 italic")(
      p(_class := "text-sm text-[#ffffff]")(
        "chronotes (short for \"chronological notes\") can come in handy when you need to take quick notes where each entry must have a timestamp associated with it."
      )
    )

  private def notes_input(note: Option[PotentialNote]): Html[Msg] =
    val (verb, focusBorderClass, bgClass) = note.flatMap(_.index) match
      case None    => ("add", "focus:outline-[#b8bb26]", "bg-[#b8bb26]")
      case Some(_) => ("update", "focus:outline-[#fe8019]", "bg-[#fe8019]")

    div(_class := "mt-6")(
      p(_class := "font-semibold")(
        s"Type note entry and press enter or click \"${verb}\""
      ),
      form(_class := "flex gap-2 items-center mt-2")(
        input(
          _class := s"flex-1 outline-2 outline-[#928374] ${focusBorderClass} text-[#ffffff] h-10 p-2",
          id           := "note-input",
          autoComplete := "off",
          attribute("data-1p-ignore", ""),
          value := note.map(_.body).getOrElse(""),
          onInput(Msg.UserEnteredNoteBody(_))
        ),
        button(
          _class := s"${bgClass} text-[#282828] disabled:bg-[#928374] text-lg p-2 font-semibold cursor-pointer",
          disabled(note.map(_.body.isEmpty).getOrElse(true)),
          onClick(Msg.UserSubmittedNewNote)
        )(
          verb
        ),
        note.map(_.body.isEmpty).getOrElse(true) match
          case true => Empty
          case false =>
            button(
              _class := "bg-[#fb4934] text-[#282828] text-lg p-2 font-semibold cursor-pointer",
              onClick(Msg.UserRequestedEditCancellation)
            )("cancel")
      )
    )

  private def notes_section(model: Model): Html[Msg] =
    if (model.notes.isEmpty) {
      div()
    } else {
      val inputs = model.notes.zipWithIndex.map(
        note_entry(model.currentNote.flatMap(_.index))
      )
      val children = List(p(_class := "text-xl mb-2")("Entries")) ++ inputs

      div(
        _class := "md:flex-1 flex flex-col gap-2 mt-4 border-2 border-dotted border-[#928374] border-opacity-10 p-4 md:overflow-y-scroll"
      )(
        children*
      )
    }

  private def note_entry(currently_edited_index: Option[Int])(
      note: Note,
      index: Int
  ): Html[Msg] =
    val isDisabled = currently_edited_index.isDefined
    val cursor = if (isDisabled) {
      "cursor-not-allowed"
    } else {
      "cursor-pointer"
    }

    div(
      _class := s"flex gap-2 text-sm items-center hover:text-[#fabd2f] hover:text-semibold"
    )(
      p()(
        note.timestamp.toLocaleTimeString
      ),
      p(_class := "flex-1")(note.body),
      button(
        _class := s"px-2 py-1 bg-[#fe8019] disabled:bg-[#928374] text-[#282828] font-semibold ${cursor} ${isDisabled}",
        title := "edit note",
        disabled(isDisabled),
        onClick(
          Msg.UserRequestedToEditNote(index)
        )
      )("✎"),
      button(
        _class := s"px-2 py-1 bg-[#fabd2f] disabled:bg-[#928374] text-[#282828] font-semibold ${cursor} ",
        title := "move timestamp backwards by a minute",
        disabled(isDisabled),
        onClick(
          Msg.UserRequestedTimeStampBeUpdated(
            index,
            TimestampUpdateType.PushBehind
          )
        )
      )("◀"),
      button(
        _class := s"px-2 py-1 bg-[#83a598] disabled:bg-[#928374] text-[#282828] font-semibold ${cursor}",
        title := "move timestamp forwards by a minute",
        disabled(isDisabled),
        onClick(
          Msg.UserRequestedTimeStampBeUpdated(
            index,
            TimestampUpdateType.PushForward
          )
        )
      )("▶"),
      button(
        _class := s"px-2 py-1 bg-[#fb4934] disabled:bg-[#928374] text-[#282828] font-semibold ${cursor}",
        title := "delete note",
        disabled(isDisabled),
        onClick(Msg.UserRequestedNoteDeletion(index))
      )("⌫")
    )

  private def all_notes(
      notes: Vector[Note],
      recentlyCopied: Boolean
  ): Html[Msg] =
    val (buttonClass, buttonText) = recentlyCopied match {
      case true  => ("bg-[#d3869b]", "copied!")
      case false => ("bg-[#b8bb26]", "copy")
    }

    if (notes.isEmpty) {
      div()
    } else {
      div(
        _class := "mt-4 md:h-1/3 md:overflow-y-scroll p-4 border-2 border-dotted border-[#928374] border-opacity-10"
      )(
        div(_class := "flex items-center")(
          p(_class := "flex-1 text-lg text-[#ffffff]")("Notes"),
          button(
            _class := s"px-2 py-1 right-0 ${buttonClass} text-[#282828] font-semibold cursor-pointer",
            title := "copy notes to clipboard",
            onClick(Msg.UserRequestedCopyToClipboard)
          )(buttonText)
        ),
        pre(
          _class := "text-sm mt-4 text-[#ffffff] overflow-x-auto",
          id     := "rendered-notes"
        )(
          notes.map(get_note_line).mkString("\n")
        )
      )
    }
}
