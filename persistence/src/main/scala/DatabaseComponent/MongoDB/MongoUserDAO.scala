package DatabaseComponent.MongoDB

import DatabaseComponent.UserDAO
import reactivemongo.api.MongoConnection
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class MongoUserDAO extends UserDAO {

  private val mongoUri = sys.env.getOrElse("MONGODB_URI", "mongodb://localhost:27017/mydb")

  val parsedUri = MongoConnection.parseURI(mongoUri)
  val connection = parsedUri.map(MongoConnection(_))
  val futureConnection = Future.fromTry(connection)

  def database = futureConnection.flatMap(_.database("mydb"))

  def gridCollection: Future[BSONCollection] = database.map(_.collection("grid"))
  def fieldCollection: Future[BSONCollection] = database.map(_.collection("field"))

  def createTables(): Future[Unit] = Future.successful(()) // No need for table creation in MongoDB

  def dropTables(): Future[Unit] = {
    for {
      db <- database
      _ <- db.drop()
    } yield ()
  }

  private def insertBoard(sizeRow: Int, sizeCol: Int): Future[String] = {
    val gridDoc = Json.obj("sizeRow" -> sizeRow, "sizeCol" -> sizeCol)
    gridCollection.flatMap(_.insert.one(gridDoc).map(_.writeErrors.headOption.map(_.errmsg).getOrElse(gridDoc._id.get.toString)))
  }

  private def insertCells(gridId: String, fieldCells: Seq[JsValue]): Future[Unit] = {
    val fieldDocs = fieldCells.map { cell =>
      Json.obj(
        "gridId" -> gridId,
        "row" -> (cell \ "row").as[Int],
        "col" -> (cell \ "col").as[Int],
        "cell" -> (cell \ "cell").as[String]
      )
    }
    fieldCollection.flatMap(_.insert(ordered = false).many(fieldDocs)).map(_ => ())
  }

  def save(game: String): Future[String] = {
    Try(Json.parse(game)) match {
      case Failure(exception) =>
        Future.failed(new IllegalArgumentException("Invalid JSON!"))
      case Success(json) =>
        val sizeRow: Int = (json \ "field" \ "sizeRow").as[Int]
        val sizeCol: Int = (json \ "field" \ "sizeCol").as[Int]
        val fieldCells: Seq[JsValue] = (json \ "field" \ "cells").asOpt[Seq[JsValue]].getOrElse(Seq.empty)

        for {
          gridId <- insertBoard(sizeRow, sizeCol)
          _ <- insertCells(gridId, fieldCells)
        } yield gridId
    }
  }

  def load(): Future[Option[String]] = {
    val loadGrids = gridCollection.flatMap(_.find(BSONDocument(), Option.empty[JsValue]).cursor[JsValue]().collect[Seq]())
    val loadFields = fieldCollection.flatMap(_.find(BSONDocument(), Option.empty[JsValue]).cursor[JsValue]().collect[Seq]())

    for {
      grids <- loadGrids
      fields <- loadFields
    } yield {
      val data = Map(
        "grids" -> Json.toJson(grids),
        "fields" -> Json.toJson(fields)
      )
      Some(Json.stringify(Json.toJson(data)))
    }
  }

  def closeDatabase(): Unit = {
    futureConnection.map(_.askClose()(scala.concurrent.duration.Duration(5, "seconds")))
  }
}
