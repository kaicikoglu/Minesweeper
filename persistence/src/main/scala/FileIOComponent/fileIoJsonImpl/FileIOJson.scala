package FileIOComponent.fileIoJsonImpl

import FieldComponent.FieldBaseImpl.Field
import FieldComponent.FieldInterface
import FileIOComponent.FileIOInterface
import play.api.libs.json.{JsObject, JsValue, Json}

import java.io.*
import scala.io.Source

class FileIOJson extends FileIOInterface:

  override def load: FieldInterface =
    var field: FieldInterface = null
    val source: Source = Source.fromFile("field.json")
    val json: JsValue = Json.parse(source.getLines().mkString)
    val sizeRow = (json \ "field" \ "sizeRow").get.toString.toInt
    val sizeCol = (json \ "field" \ "sizeCol").get.toString.toInt
    sizeRow match {
      case 8 =>
        field = new Field(8, 8)
      case 16 =>
        field = new Field(16, 16)
      case 32 =>
        field = new Field(32, 16)
    }
    ( 0 until sizeRow * sizeCol).map(index =>
      val row = (json \\ "row")(index).as[Int]
      val col = (json \\ "col")(index).as[Int]
      val first = field.toStone((json \\ "first")(index).as[String])
      val second = field.toStone((json \\ "second")(index).as[String])
      val third = (json \\ "third")(index).as[Int]
      field.setCell(row, col, (first, second, third))
    )
    source.close()
    field


  override def save(field: FieldInterface): Unit =
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(gameToJson(field)))
    pw.close()

  def gameToJson(field: FieldInterface): JsObject =
    Json.obj(
      "field" -> Json.obj(
        "sizeRow" -> field.rows,
        "sizeCol" -> field.cols,
        "cells" -> Json.toJson(
          for {
            row <- 0 until field.rows
            col <- 0 until field.cols
          } yield {
            Json.obj(
              "row" -> row,
              "col" -> col,
              "cell" -> Json.obj(
                "first" -> Json.toJson(field.getCell(row, col)._1.toString),
                "second" -> Json.toJson(field.getCell(row, col)._2.toString),
                "third" -> Json.toJson(field.getCell(row, col)._3)
              )
            )
          }
        )
      )
    )
