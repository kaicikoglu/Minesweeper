package api

import DatabaseComponent.Slick.SlickUserDAO
import DatabaseComponent.UserDAO
import FieldComponent.FieldInterface
import FileIOComponent.FileIOInterface
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.google.inject.{Guice, Injector}
import module.PersistenceModule
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PersistenceApi(var field: FieldInterface, var fileIO: FileIOInterface)(implicit
    val system: ActorSystem,
    val materializer: Materializer
) {

  val injector: Injector = Guice.createInjector(new PersistenceModule)

  implicit val ec: ExecutionContext = system.dispatcher
  val routes: Route = pathPrefix("fileIo") {
    pathEnd {
      post {
        entity(as[String]) { json =>
          val jsonValue: JsValue = Json.parse(json)
          val fieldValue: String = (jsonValue \ "field").as[JsValue].toString()
          field = field.jsonToField(fieldValue)
          fileIO.save(field)

          val db = injector.getInstance(classOf[UserDAO])

          val dbOperations: Future[Unit] = for {
            _ <- db.delete()
            _ <- db.create()
            _ <- db.save(fieldValue)
          } yield ()

          onComplete(dbOperations) {
            case Success(_) =>
              logger.info("Field saved to database")
              complete(HttpEntity(ContentTypes.`application/json`, field.toJson.toString())) // Explicit JSON response
            case Failure(exception) =>
              logger.error("Error saving field to database", exception)
              complete(
                HttpEntity(
                  ContentTypes.`application/json`,
                  s"""{"error": "Failed to save field: ${exception.getMessage}"}"""
                )
              )
          }
        }
      }
    } ~
      path("load") {
        get {
          val db = injector.getInstance(classOf[UserDAO])

          onComplete(db.load()) {
            case Success(Some(value)) =>
              field = field.jsonToField(value)
              logger.info("Field loaded from database")
              complete(HttpEntity(ContentTypes.`application/json`, field.toJson.toString())) // Explicit JSON response

            case Success(None) =>
              logger.warn("No field found in database")
              complete(HttpEntity(ContentTypes.`application/json`, """{"error": "No field found in database"}"""))

            case Failure(exception) =>
              logger.error("Error loading field from database", exception)
              complete(
                HttpEntity(
                  ContentTypes.`application/json`,
                  s"""{"error": "Failed to load field: ${exception.getMessage}"}"""
                )
              )
          }
        }
      }
  }
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
}
