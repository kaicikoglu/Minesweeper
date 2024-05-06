package api

import FieldComponent.FieldBaseImpl.{DifficultyFactory, Stone}
import FieldComponent.FieldInterface
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsValue, Json}

class ModelApi(var field: FieldInterface) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val routes: Route = pathPrefix("field") {
    pathEnd {
      get {
        complete(field.toJson.toString())
      } ~
        post {
          entity(as[String]) { json =>
            val jsonValue: JsValue = Json.parse(json)
            val fieldValue: String = (jsonValue \ "field").as[JsValue].toString()
            val row: Int = (jsonValue \ "row").as[Int]
            val col: Int = (jsonValue \ "col").as[Int]

            field = field.jsonToField(fieldValue)

            complete(field.toJson.toString())
          }
        }
    } ~
      path("calculateBombs") {
        get {
          complete(Json.obj("bombAmount" -> field.calculateBombAmount()).toString())
        }
      } ~
      path("setBombs") {
        post {
          entity(as[String]) { json =>
            val jsonValue = Json.parse(json)
            val bombAmount = (jsonValue \ "bombAmount").as[Int]
            complete(field.toJson.toString())
          }
        }
      } ~
      path("getCell") {
        get {
          parameters("x".as[Int], "y".as[Int]) { (x, y) =>
            complete(
              Json
                .obj(
                  "first" -> field.getCell(x, y)._1.toString,
                  "second" -> field.getCell(x, y)._2.toString,
                  "third" -> field.getCell(x, y)._3.toString
                )
                .toString()
            )
          }
        }
      } ~
      path("flagsLeft") {
        get {
          complete(Json.obj("flagsLeft" -> field.flagsLeft()).toString())
        }
      } ~
      path("createNew") {
        post {
          parameters("x".as[String]) { x =>
            field = DifficultyFactory.apply(x).run
            field = field.setBombs(field.calculateBombAmount())
            field = field.showValues()
            complete(field.toJson.toString())
          }
        }
      } ~
      path("setFlag") {
        post {
          entity(as[String]) { json =>
            val jsonValue = Json.parse(json)
            val x = (jsonValue \ "x").as[Int]
            val y = (jsonValue \ "y").as[Int]
            field = field.setFlag(x, y)
            complete(field.toJson.toString())
          }
        }
      } ~
      path("revealValue") {
        post {
          entity(as[String]) { json =>
            val jsonValue: JsValue = Json.parse(json)
            val x: Int = (jsonValue \ "x").as[Int]
            val y: Int = (jsonValue \ "y").as[Int]

            val updatedField = revealValue(x, y)
            complete(updatedField.toJson.toString())
          }
        }
      }
  }

  private def revealValue(x: Int, y: Int): FieldInterface =
    if field.getCell(x, y)._1 != Stone.NotTracked then field
    else field.revealValue(x, y)
}
