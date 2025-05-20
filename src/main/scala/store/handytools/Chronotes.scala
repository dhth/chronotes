package store.handytools

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("Chronotes")
object Chronotes extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg =
    Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (
      Model.init(),
      Cmd.Batch(
        Effects.applyPreviousTheme,
        Effects.startSystemThemeTracking,
        Effects.loadPreviousNotesFromStorage
      )
    )

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) = Update.update(model)

  def view(model: Model): Html[Msg] =
    div(
      className := "dark:bg-neutral-800 bg-neutral-100 dark:text-neutral-100 text-neutral-800"
    )(
      div(className := "w-4/5 max-sm:w-full max-sm:px-4 mx-auto")(
        View.mainSection(model)
      )
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None
