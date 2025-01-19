package tuiView

import FieldComponent.FieldBaseImpl.Coordinates
import FieldComponent.FieldInterface
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.ContentTypes.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import kafkaConsumer.Consumer
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI(coreServiceUrl: String = "http://localhost:8082")(implicit system: ActorSystem, materializer: Materializer) {

  def run(): Unit = {
    // Start Kafka Consumer
    Consumer(printCurrentState)

    // Create input source and run the stream
    inputSource.via(inputProcessingFlow).runWith(Sink.ignore)
  }

  // Kafka Consumer updates the state of the TUI
  private def printCurrentState(field: FieldInterface): Unit =
    println(field.toString)

  // Reactive Streams: Source for user input
  private def inputSource: Source[String, _] = Source.unfoldAsync(()) { _ =>
    Future {
      val input = readLine()
      if (input == "q") None else Some(((), input))
    }
  }

  // Reactive Streams: Flow for processing user input
  private def inputProcessingFlow: Flow[String, Unit, _] = Flow[String].mapAsync(1) { input =>
    if (input.length == 1 && input.forall(_.isDigit)) { // Single-character commands
      checkInput(input) match {
        case Success(_) => input match {
          case "q" => Future.successful(System.exit(0))
          case "u" => sendPostRequest("/controller/undo")
          case "r" => sendPostRequest("/controller/redo")
          case "s" => sendPostRequest("/controller/save")
          case "l" => sendPostRequest("/controller/load")
          case "1" | "2" | "3" => sendPostRequest(s"/controller/createNewField?difficulty=$input")
        }
        case Failure(exception) =>
          println(exception.getMessage)
          Future.unit
      }
    } else if (input.length == 2 && input.forall(_.isDigit)) { // Two-character input (coordinates)
      parseInput(input) match {
        case Some(coordinates) => sendRevealValueRequest(coordinates)
        case None =>
          println("Invalid coordinates format. Expected two digits.")
          Future.unit
      }
    } else if (input.length == 3 && input(2).toLower == 'f' && input.substring(0, 2).forall(_.isDigit)) { // Flagging input
      parseInput(input.substring(0, 2)) match {
        case Some(coordinates) => sendSetFlagRequest(coordinates)
        case None =>
          println("Invalid coordinates format for setting a flag. Expected two digits.")
          Future.unit
      }
    } else { // Invalid input
      println("Invalid input format.")
      Future.unit
    }
  }


  // Parse input to extract coordinates
  private def parseInput(input: String): Option[Coordinates] = {
    if (input.length == 2 && input.forall(_.isDigit)) { // Ensure the input is exactly two digits
      Try {
        val x = input.charAt(0).toString.toInt
        val y = input.charAt(1).toString.toInt
        new Coordinates(x, y)
      }.toOption
    } else {
      None
    }
  }


  // Validate single-character commands
  private def checkInput(input: String): Try[String] = {
    val pattern = "^(q|u|r|s|l|[1-3])$".r
    input match {
      case pattern(_*) => Success(input)
      case _ => Failure(new IllegalArgumentException("Invalid input"))
    }
  }

  // Send HTTP POST request for generic commands
  private def sendPostRequest(endpoint: String): Future[Unit] = {
    val url = s"$coreServiceUrl$endpoint"
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = url)).flatMap(handleResponse)
  }

  // Handle HTTP response
  private def handleResponse(response: HttpResponse): Future[Unit] = {
    if (response.status.isSuccess()) {
      Unmarshal(response.entity).to[String].map { body =>
        println(body)
      }
    } else {
      println(s"Request failed with status: ${response.status}")
      Future.unit
    }
  }

  // Send HTTP POST request to reveal a value
  private def sendRevealValueRequest(coordinates: Coordinates): Future[Unit] = {
    val url = s"$coreServiceUrl/controller/doAndPublish"
    val json = Json.obj("x" -> coordinates.x, "y" -> coordinates.y).toString()
    val entity = HttpEntity(`application/json`, json)
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = url, entity = entity)).flatMap(handleResponse)
  }

  // Send HTTP POST request to set a flag
  private def sendSetFlagRequest(coordinates: Coordinates): Future[Unit] = {
    val url = s"$coreServiceUrl/controller/setFlag"
    val json = Json.obj("x" -> coordinates.x, "y" -> coordinates.y).toString()
    val entity = HttpEntity(`application/json`, json)
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = url, entity = entity)).flatMap(handleResponse)
  }
}
