package DatabaseComponent.Slick

import DatabaseComponent.Slick.Tables.{FieldTable, GridTable}
import DatabaseComponent.UserDAO
import play.api.libs.json.{JsValue, Json}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class SlickUserDAO extends UserDAO {

  val field = TableQuery[FieldTable]
  private val databaseDB: String = sys.env.getOrElse("POSTGRES_DATABASE", "postgres")
  private val databaseUser: String = sys.env.getOrElse("POSTGRES_USER", "postgres")
  private val databasePassword: String = sys.env.getOrElse("POSTGRES_PASSWORD", "postgres")
  private val databasePort: String = sys.env.getOrElse("POSTGRES_PORT", "5432")
  private val databaseHost: String = sys.env.getOrElse("POSTGRES_HOST", "postgresdb")
  private val databaseUrl =
    s"jdbc:postgresql://$databaseHost:$databasePort/$databaseDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true"
  private val grid = TableQuery[GridTable]

  val database = Database.forURL(
    url = databaseUrl,
    driver = "org.postgresql.Driver",
    user = databaseUser,
    password = databasePassword
  )

  def create(): Future[Unit] = {
    val createGridTableAction = grid.schema.createIfNotExists
    val createFieldTableAction = field.schema.createIfNotExists

    val combinedAction = for {
      _ <- createGridTableAction
      _ <- createFieldTableAction
    } yield ()

    database.run(combinedAction)
  }

  def delete(): Future[Unit] = {
    val dropFieldTableAction = field.schema.dropIfExists
    val dropGridTableAction = grid.schema.dropIfExists

    val combinedAction = DBIO.seq(
      dropFieldTableAction,
      dropGridTableAction
    )

    database.run(combinedAction)
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

  private def insertBoard(sizeRow: Int, sizeCol: Int): Future[Int] = {
    database.run((grid returning grid.map(_.gridId)) += (0, sizeRow, sizeCol))
  }

  private def insertCells(gridId: Int, fieldCells: Seq[JsValue]): Future[Option[Int]] = {
    val fieldInsertions = fieldCells.map { cell =>
      val r = (cell \ "row").as[Int]
      val c = (cell \ "col").as[Int]
      val first = (cell \ "cell" \ "first").as[String]
      val second = (cell \ "cell" \ "second").as[String]
      val third = (cell \ "cell" \ "third").as[Int]
      (0, gridId, r, c, first, second, third) // Updated to match the FieldTable schema
    }
    database.run(field ++= fieldInsertions)
  }

  def load(): Future[Option[String]] = {
    val loadGrids = grid.result.headOption
    val loadFields = field.result

    for {
      gridOpt <- database.run(loadGrids)
      fields <- database.run(loadFields)
    } yield {
      gridOpt match {

        case Some((_, sizeRow, sizeCol)) =>
          // Map fields data to JSON structure
          val cellsJson = fields.map { case (_, _, row, col, first, second, third) =>
            Json.obj(
              "row" -> row,
              "col" -> col,
              "cell" -> Json.obj(
                "first" -> first,
                "second" -> second,
                "third" -> third
              )
            )
          }
          // Construct JSON with top-level "field" key
          val fieldJson = Json.obj(
            "field" -> Json.obj(
              "sizeRow" -> sizeRow,
              "sizeCol" -> sizeCol,
              "cells" -> cellsJson
            )
          )
          Some(Json.stringify(fieldJson))

        case None =>
          None // No grid found, return None
      }
    }
  }

  def closeDatabase(): Unit = {
    database.close()
  }
}
