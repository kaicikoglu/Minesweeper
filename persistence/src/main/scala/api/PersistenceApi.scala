package api

import DatabaseComponent.Slick.SlickUserDAO
import FieldComponent.FieldInterface
import FileIOComponent.FileIOInterface
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

class PersistenceApi(var field: FieldInterface, var fileIO: FileIOInterface) {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val routes: Route = pathPrefix("fileIo") {
    pathEnd {
      post {
        entity(as[String]) { json =>
          val jsonValue: JsValue = Json.parse(json)
          val fieldValue: String = (jsonValue \ "field").as[JsValue].toString()
          field = field.jsonToField(fieldValue)
          fileIO.save(field)
          
          val db = SlickUserDAO()
          db.dropTables().onComplete{
            case Success(_) => 
              logger.info("Tables dropped")
            case Failure(exception) => 
              logger.error("Tables could not be dropped", exception)
          }

          db.createTables().onComplete {
            case Success(_) =>
              logger.info("Tables created")
              db.save(fieldValue).onComplete {
                case Success(_) => logger.info("Field saved")
                case Failure(exception) => logger.error("Field not saved", exception)
              }

            case Failure(exception) => logger.error("Tables not created", exception)
          }
          
          complete(field.toJson.toString())
        }
      }
    } ~
      path("load") {
        get {
          val db = SlickUserDAO()
          db.load().onComplete {
            case Success(value) =>
              field = field.jsonToField(value.get)
              logger.info("Field loaded")
            case Failure(exception) => logger.error("Field not loaded", exception)
          }
          
          complete(fileIO.load.toJson.toString())
        }
      }
  }
}
