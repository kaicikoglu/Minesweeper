package api

import FieldComponent.FieldBaseImpl.Coordinates
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import controllerComponent.ControllerInterface
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

class CoreApi(var controller: ControllerInterface) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val routes: Route = pathPrefix("controller") {
    path("undo") {
      post {
        complete(controller.undo.toString)
      }
    } ~
      path("redo") {
        post {
          complete(controller.redo.toString)
        }
      } ~
      path("save") {
        post {
          complete(controller.save.toString)
        }
      } ~
      path("load") {
        post {
          complete(controller.load.toString)
        }
      } ~
      path("reveal") {
        post {
          entity(as[String]) { json =>
            val coordinates = parseCoordinatesReveal(json)
            complete(controller.revealValue(coordinates).toString)
          }
        }
      } ~
      path("setFlag") {
        post {
          entity(as[String]) { json =>
            val coordinates = parseCoordinatesFlag(json)
            complete(controller.setFlag(coordinates).toString)
          }
        }
      } ~
      path("flagsLeft") {
        get {
          complete(controller.flagsLeft().toString)
        }
      } ~
      path("createNewField") {
        parameter("difficulty") { difficulty =>
          post {
            complete(controller.createNewField(difficulty).toString)
          }
        }
      } ~
      // Endpoint to calculate the bomb amount for the current game
      path("calculateBombAmount") {
        get {
          complete(controller.calculateBombAmount().toString)
        }
      } ~
      // Endpoint to set bombs in the field
      path("setBombs") {
        post {
          entity(as[String]) { json =>
            val bombAmount = (Json.parse(json) \ "bombAmount").as[Int]
            complete(controller.setBombs(bombAmount).toString)
          }
        }
      } ~
      // Endpoint to get cell information
      path("getCell") {
        parameters("x".as[Int], "y".as[Int]) { (x, y) =>
          get {
            val cellInfo = controller.getCell(x, y)
            complete(
              Json
                .obj(
                  "first" -> cellInfo._1.toString,
                  "second" -> cellInfo._2.toString,
                  "third" -> cellInfo._3
                )
                .toString()
            )
          }
        }
      }
  }

  // Helper function to parse coordinates
  private def parseCoordinatesFlag(json: String): Coordinates = {
    val parsedJson = Json.parse(json)
    val x = (parsedJson \ "x").as[Int]
    val y = (parsedJson \ "y").as[Int]
    val f = (parsedJson \ "f").as[String]
    Coordinates(x, y, f.toCharArray.head)
  }

  // Helper function to parse coordinates
  private def parseCoordinatesReveal(json: String): Coordinates = {
    val parsedJson = Json.parse(json)
    val x = (parsedJson \ "x").as[Int]
    val y = (parsedJson \ "y").as[Int]
    Coordinates(x, y, ' ')
  }
}
