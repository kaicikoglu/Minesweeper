package DatabaseComponent.MongoDB

import DatabaseComponent.UserDAO
import lib.Servers.mongoServer
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, SingleObservableFuture}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class MongoUserDAO extends UserDAO {
  private val mongoServerParts = mongoServer.split(":")
  private val host = mongoServerParts(0)
  private val port = mongoServerParts(1)

  private val databaseDB: String = sys.env.getOrElse("MONGO_DB", "mongo")
  private val databaseUser: String = sys.env.getOrElse("MONGO_USERNAME", "root")
  private val databasePassword: String = sys.env.getOrElse("MONGO_PASSWORD", "mongo")
  private val databasePort: String = sys.env.getOrElse("MONGO_PORT", port)
  private val databaseHost: String = sys.env.getOrElse("MONGO_HOST", host)

  private val databaseURI: String =
    s"mongodb://$databaseUser:$databasePassword@$databaseHost:$databasePort/?authSource=admin"
  private val client: MongoClient = MongoClient(databaseURI)
  private val db: MongoDatabase = client.getDatabase(databaseDB)
  private val gameCollection: MongoCollection[Document] = db.getCollection("game")

  override def delete(): Future[Unit] = {
    gameCollection.drop().toFuture().map(_ => ())
  }

  override def create(): Future[Unit] = {
    db.createCollection("game").toFuture().map(_ => ())
  }

  override def save(game: String): Future[Int] = {
    println("save called MongoUserDAO")

    Try(Json.parse(game)) match {
      case Failure(exception) =>
        Future.failed(new IllegalArgumentException("Invalid JSON format"))
      case Success(_) =>
        val doc = Document(game)
        gameCollection.insertOne(doc).toFuture().map(_ => 1)
    }
  }

  override def load(): Future[Option[String]] = {
    gameCollection.find().sort(Document("_id" -> -1)).first().toFuture().map {
      case null => None
      case doc => Some(doc.toJson()) // Return the JSON representation as a string
    }
  }

  override def closeDatabase(): Unit = {
    client.close()
  }
}
