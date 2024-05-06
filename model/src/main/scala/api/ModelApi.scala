package api

import FieldComponent.FieldBaseImpl.DifficultyFactory
import FieldComponent.FieldInterface
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import play.api.libs.json.{JsValue, Json}

class ModelApi(var field: FieldInterface) {

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
      path ("createNew") {
        post {
          parameters("x".as[String]) {
            x =>
              field = DifficultyFactory.apply(x).run
              field = field.setBombs(field.calculateBombAmount())
              field = field.showValues()
              complete(field.toJson.toString())
          }
        }
      }
  }
}
