import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / semanticdbEnabled    := true

lazy val circeVersion = "0.14.13"

lazy val chronotes =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name         := "chronotes",
      version      := "0.1.0",
      scalaVersion := "3.7.0",
      organization := "store.handytools",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian-io"     % "0.14.0",
        "io.circe"        %%% "circe-core"    % circeVersion,
        "io.circe"        %%% "circe-generic" % circeVersion,
        "io.circe"        %%% "circe-parser"  % circeVersion
      ),
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
      scalafixOnCompile := true,
      semanticdbEnabled := true,
      semanticdbVersion := scalafixSemanticdb.revision,
      autoAPIMappings   := true
    )
    .settings(
      logo := List(
        "",
        "chronotes (v" + version.value + ")",
        ""
      ).mkString("\n"),
      usefulTasks := Seq(
        UsefulTask(
          "fastLinkJS",
          "Rebuild the JS (use during development)"
        ).noAlias,
        UsefulTask(
          "fullLinkJS",
          "Rebuild the JS and optimise (use in production)"
        ).noAlias
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )
