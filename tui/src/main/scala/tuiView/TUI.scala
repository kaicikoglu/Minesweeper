package tuiView

import FieldComponent.FieldBaseImpl.Coordinates
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.ContentTypes.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI(coreServiceUrl: String = "http://localhost:8082")(implicit system: ActorSystem, materializer: Materializer) {

  private val continue = true

  def run(): Any =
    getInputAndPrintLoop

  // Parse input coordinates
  def parseInput(input: String): Option[Coordinates] =
    Try {
      val x = input.charAt(0).toString.toInt
      val y = input.charAt(1).toString.toInt
      new Coordinates(x, y)
    }.toOption

  // TUI update loop
  private def getInputAndPrintLoop: Any = {
    val input = readLine()
    input.length match {
      case 1 =>
        checkInput(input) match {
          case Success(value) =>
            input match {
              case "q"             => System.exit(0)
              case "u"             => sendPostRequest("/controller/undo")
              case "r"             => sendPostRequest("/controller/redo")
              case "s"             => sendPostRequest("/controller/save")
              case "l"             => sendPostRequest("/controller/load")
              case "1" | "2" | "3" => sendPostRequest(s"/controller/createNewField?difficulty=$input")
            }
          case Failure(exception) => println(exception.getMessage)
        }
      case 2 =>
        parseInput(input) match {
          case None       => System.exit(0)
          case Some(move) => sendRevealValueRequest(move)
        }
      case 3 =>
        val chars = input.toCharArray
        val helpFlag = chars(2).toString
        helpFlag match {
          case "F" | "f" =>
            parseInput(input) match {
              case None       => System.exit(0)
              case Some(move) => sendSetFlagRequest(move)
            }
          case _ => println("Invalid input")
        }
      case _ => println("Invalid input")
    }

    if (continue) getInputAndPrintLoop
  }

  // Helper method to validate single-character input
  private def checkInput(input: String): Try[String] = {
    val pattern = "^(q|u|r|s|l|[1-3])$".r
    input match {
      case pattern(_*) => Success(input)
      case _           => Failure(new IllegalArgumentException("Invalid input"))
    }
  }

  // HTTP POST helper to send general requests without body
  private def sendPostRequest(endpoint: String): Unit = {
    val url = s"$coreServiceUrl$endpoint"
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = url)).onComplete {
      case Success(response) =>
        handleResponse(response)
      case Failure(ex) =>
        println(s"Failed to send request: $ex")
    }
  }

  // HTTP POST request to reveal a cell (with coordinates)
  private def sendRevealValueRequest(coordinates: Coordinates): Unit = {
    val url = s"$coreServiceUrl/controller/reveal"
    val json = Json.obj("x" -> coordinates.x, "y" -> coordinates.y).toString()
    val entity = HttpEntity(`application/json`, json)
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = url, entity = entity)).onComplete {
      case Success(response) =>
        handleResponse(response)
      case Failure(ex) =>
        println(s"Failed to send request: $ex")
    }
  }

  // HTTP POST request to set a flag on a cell (with coordinates)
  private def sendSetFlagRequest(coordinates: Coordinates): Unit = {
    val url = s"$coreServiceUrl/controller/setFlag"
    val json = Json.obj("x" -> coordinates.x, "y" -> coordinates.y).toString()
    val entity = HttpEntity(`application/json`, json)
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = url, entity = entity)).onComplete {
      case Success(response) =>
        handleResponse(response)
      case Failure(ex) =>
        println(s"Failed to send request: $ex")
    }
  }
  // HTTP GET request for flagsLeft
  private def sendFlagsLeftRequest(): Unit = {
    val url = s"$coreServiceUrl/controller/flagsLeft"
    Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = url)).onComplete {
      case Success(response) =>
        handleResponse(response)
      case Failure(ex) =>
        println(s"Failed to send request: $ex")
    }
  }
  // Response handler
  private def handleResponse(response: HttpResponse): Unit = {
    if (response.status.isSuccess()) {
      Unmarshal(response.entity).to[String].foreach { body =>
        println(body) // Print the response body from the core service
      }
    } else {
      println(s"Request failed with status: ${response.status}")
    }
  }
}
