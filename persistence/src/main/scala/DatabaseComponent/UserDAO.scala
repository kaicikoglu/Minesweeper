package DatabaseComponent

import scala.concurrent.Future

trait UserDAO {
  def dropTables(): Future[Unit]
  def createTables(): Future[Unit]
  def save(game: String): Future[Int]
  def load(): Future[Option[String]]
  def closeDatabase(): Unit
}
