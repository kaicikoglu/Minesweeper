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
            val coordinates = parseCoordinates(json)
            complete(controller.revealValue(coordinates).toString)
          }
        }
      } ~
      path("setFlag") {
        post {
          entity(as[String]) { json =>
            val coordinates = parseCoordinates(json)
            complete(controller.setFlag(coordinates).toString)
          }
        }
      } ~
      path("flagsLeft"){
        get {
            complete(controller.flagsLeft().toString)
        }
      }
  }

  private def parseCoordinates(json: String): Coordinates = {
    val parsedJson = Json.parse(json)
    val x = (parsedJson \ "x").as[Int]
    val y = (parsedJson \ "y").as[Int]
    val f = (parsedJson \ "f").as[String]
    Coordinates(x, y, f.toCharArray.head)
  }
}
