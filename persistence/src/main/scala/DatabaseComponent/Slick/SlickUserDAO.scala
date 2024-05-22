package DatabaseComponent.Slick

import DatabaseComponent.Slick.Tables.{FieldTable, GridTable}
import DatabaseComponent.UserDAO
import play.api.libs.json.{JsValue, Json}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class SlickUserDAO extends UserDAO {

  private val databaseDB: String = sys.env.getOrElse("POSTGRES_DATABASE", "postgres")
  private val databaseUser: String = sys.env.getOrElse("POSTGRES_USER", "postgres")
  private val databasePassword: String = sys.env.getOrElse("POSTGRES_PASSWORD", "postgres")
  private val databasePort: String = sys.env.getOrElse("POSTGRES_PORT", "5432")
  private val databaseHost: String = sys.env.getOrElse("POSTGRES_HOST", "database")
  private val databaseUrl = s"jdbc:postgresql://$databaseHost:$databasePort/$databaseDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true"

  val database = Database.forURL(
    url = databaseUrl,
    driver = "org.postgresql.Driver",
    user = databaseUser,
    password = databasePassword
  )

  val grid = TableQuery[GridTable]
  val field = TableQuery[FieldTable]

  def createTables(): Future[Unit] = {
    val createGridTableAction = grid.schema.createIfNotExists
    val createFieldTableAction = field.schema.createIfNotExists

    val combinedAction = DBIO.seq(
      createGridTableAction,
      createFieldTableAction
    )

    database.run(combinedAction)
  }
  
  def dropTables(): Future[Unit] = {
    val dropFieldTableAction = field.schema.dropIfExists
    val dropGridTableAction = grid.schema.dropIfExists

    val combinedAction = DBIO.seq(
      dropFieldTableAction,
      dropGridTableAction
    )

    database.run(combinedAction)
  }

  private def insertBoard(sizeRow: Int, sizeCol: Int): Future[Int] = {
    database.run((grid returning grid.map(_.gridId)) += (0, sizeRow, sizeCol))
  }

  private def insertCells(gridId: Int, fieldCells: Seq[JsValue]): Future[Option[Int]] = {
    val fieldInsertions = fieldCells.map { cell =>
      val r = (cell \ "row").as[Int]
      val c = (cell \ "col").as[Int]
      val stone = (cell \ "cell").as[String]
      (0, gridId, r, c, stone) // Correcting to match the FieldTable schema
    }
    database.run(field ++= fieldInsertions)
  }

  def save(game: String): Future[Int] = {
    Try(Json.parse(game)) match {
      case Failure(exception) =>
        Future.failed(new IllegalArgumentException("Invalid JSON!"))
      case Success(json) =>
        val sizeRow: Int = (json \ "field" \ "sizeRow").as[Int]
        val sizeCol: Int = (json \ "field" \ "sizeCol").as[Int]
        val fieldCells: Seq[JsValue] = (json \ "field" \ "cells").asOpt[Seq[JsValue]].getOrElse(Seq.empty)

        for {
          gridId <- insertBoard(sizeRow, sizeCol)
          fieldId <- insertCells(gridId, fieldCells)
        } yield fieldId.getOrElse(-1)
    }
  }

  def load(): Future[Option[String]] = {
    val loadGrids = grid.result
    val loadFields = field.result

    for {
      grids <- database.run(loadGrids)
      fields <- database.run(loadFields)
    } yield {
      val data = Map(
        "grids" -> Json.toJson(grids),
        "fields" -> Json.toJson(fields)
      )
      Some(Json.stringify(Json.toJson(data)))
    }
  }

  def closeDatabase(): Unit = {
    database.close()
  }
}
