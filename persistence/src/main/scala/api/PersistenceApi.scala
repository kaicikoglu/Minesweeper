package api

import FieldComponent.FieldInterface
import FileIOComponent.FileIOInterface
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsValue, Json}

class PersistenceApi(var field: FieldInterface, var fileIO: FileIOInterface) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val routes: Route = pathPrefix("fileIo") {
    pathEnd {
      post {
        entity(as[String]) { json =>
          val jsonValue: JsValue = Json.parse(json)
          val fieldValue: String = (jsonValue \ "field").as[JsValue].toString()
          field = field.jsonToField(fieldValue)
          fileIO.save(field)
          complete(field.toJson.toString())
        }
      }
    } ~
      path("load") {
        get {
          complete(fileIO.load.toJson.toString())
        }
      }
  }
}
