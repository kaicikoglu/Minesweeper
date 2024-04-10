package de.htwg.se.minesweeper.aview

import de.htwg.se.minesweeper.controller.controllerComponent.*
import de.htwg.se.minesweeper.model.FieldComponent.FieldBaseImpl.Coordinates
import de.htwg.se.minesweeper.util.{Event, Observer}

import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI(controller: ControllerInterface) extends Observer:
  controller.add(this)
  private var continue = true
  def run(): Any =
    controller.setBombs(controller.calculateBombAmount())
    getInputAndPrintLoop

  override def update(e: Event): Unit =
    e match
      case Event.Quit => continue = false
      case Event.Move =>
        println("Flaggen verfügbar:" + controller.flagsLeft())
        println(controller.toString)

  def parseInput(input: String): Option[Coordinates] =
    val chars = input.toCharArray
    val x = chars(0).toString.toInt
    val y = chars(1).toString.toInt
    Some(new Coordinates(x, y))

  private def getInputAndPrintLoop: Any =
    val input = readLine
    input.length match
      case 1 =>
        checkInput(input) match
          case Success(value) =>
            input match
              case "q" => System.exit(0)
              case "u" => controller.doAndPublish(controller.undo)
              case "r" => controller.doAndPublish(controller.redo)
              case "s" => controller.doAndPublish(controller.save)
              case "l" => controller.doAndPublish(controller.load)
              case "1" => controller.doAndPublish(controller.createNewField("1"))
              case "2" => controller.doAndPublish(controller.createNewField("2"))
              case "3" => controller.doAndPublish(controller.createNewField("3"))
          case Failure(exception) => println(exception.getMessage)
      case 2 =>
        parseInput(input) match
          case None       => System.exit(0)
          case Some(move) => controller.doAndPublish(controller.revealValue, move)
      case 3 =>
        val chars = input.toCharArray
        val helpFlag = chars(2).toString
        helpFlag match
          case "f" =>
          case "F" =>
            parseInput(input) match
              case None       => System.exit(0)
              case Some(move) => controller.doAndPublish(controller.setFlag, move);
          case _ => println("Ungültige eingabe")
      case _ => println("Ungültige eingabe")

    if continue then getInputAndPrintLoop

  private def checkInput(input: String): Try[String] =
    val pattern = "^(q|u|r|s|l|[1-3])$".r
    input match
      case pattern(_*) => Success(input)
      case _           => Failure(IllegalArgumentException("Invalid input"))
