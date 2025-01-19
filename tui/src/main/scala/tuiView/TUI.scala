package tuiView

import FieldComponent.FieldBaseImpl.Coordinates
import controllerComponent.*
import lib.{Event, Observer}

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

  def parseInput(input: String): Try[Coordinates] = Try {
    val chars = input.toCharArray
    val x = chars(0).toString.toInt
    val y = chars(1).toString.toInt
    new Coordinates(x, y)
  }

  private def getInputAndPrintLoop: Try[Unit] =
    def processSingleCharInput(input: String): Try[Unit] =
      for
        validInput <- checkInput(input)
      yield
        validInput match
          case "q" => System.exit(0)
          case "u" => controller.doAndPublish(controller.undo)
          case "r" => controller.doAndPublish(controller.redo)
          case "s" => controller.doAndPublish(controller.save)
          case "l" => controller.doAndPublish(controller.load)
          case "1" => controller.doAndPublish(controller.createNewField("1"))
          case "2" => controller.doAndPublish(controller.createNewField("2"))
          case "3" => controller.doAndPublish(controller.createNewField("3"))
          case _ => println("Ungültige Eingabe")


    def processDoubleCharInput(input: String): Try[Unit] = {
      for {
        move <- parseInput(input)
      } yield controller.doAndPublish(controller.revealValue, move)
    }

    def processTripleCharInput(input: String): Try[Unit] = {
      val chars = input.toCharArray
      val helpFlag = chars(2).toString

      helpFlag match {
        case "f" | "F" =>
          for {
            move <- parseInput(input)
          } yield controller.doAndPublish(controller.setFlag, move)
        case _ => Try(println("Ungültige Eingabe"))
      }
    }

    val input = readLine

    val result = input.length match {
      case 1 => processSingleCharInput(input)
      case 2 => processDoubleCharInput(input)
      case 3 => processTripleCharInput(input)
      case _ => Try(println("Ungültige Eingabe"))
    }

    result.flatMap(_ => if (continue) getInputAndPrintLoop else Try(()))

  private def checkInput(input: String): Try[String] =
    val pattern = "^(q|u|r|s|l|[1-3])$".r
    input match
      case pattern(_*) => Success(input)
      case _ => Failure(IllegalArgumentException("Invalid input"))
