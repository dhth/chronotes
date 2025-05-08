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
    (Model.init(), Effects.loadUserTheme())

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) = Update.update(model)

  def view(model: Model): Html[Msg] =
    val dark = model.theme match
      case Theme.Light => ""
      case Theme.Dark  => "dark"

    div(
      className := s"${dark} bg-[#282828] text-[#fbf1c7]"
    )(
      div(className := "w-4/5 max-sm:w-full max-sm:px-4 mx-auto")(
        View.mainSection(model)
      )
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None
