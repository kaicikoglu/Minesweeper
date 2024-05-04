package FieldComponent.FieldBaseImpl

import FieldComponent.FieldInterface
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.util.Random as r

case class Field(matrix: Matrix[Stone, Stone, Int]) extends FieldInterface:
  val cols: Int = matrix.colNum
  val rows: Int = matrix.rowNum
  val eol: String = sys.props("line.separator")

  def this(rows: Int = 3, cols: Int = 3, filling: (Stone, Stone, Int) = (Stone.NotTracked, Stone.EmptyTracked, 0)) =
    this(new Matrix[Stone, Stone, Int](rows, cols, filling))

  override def toString: String = matchfield()

  def matchfield(cellWidth: Int = 3): String =
    (" " * (cellWidth + 1))
      .+(
        (0 until cols)
          .map(x => if x < 10 then x.toString + " " * cellWidth else x.toString + " " * (cellWidth - 1))
          .mkString
      )
      .+("\n")
      .+(
        (0 until rows)
          .map(x =>
            if x < 10 then x.toString + " " + cells(x, cellWidth) + " " + x.toString + "\n"
            else x.toString + cells(x, cellWidth) + " " + x.toString + "\n"
          )
          .mkString(
            "  " + firstBar(cellWidth, cols),
            "  " + bar(cellWidth, cols),
            "  " + lastBar(cellWidth, cols)
          )
      )
      .+(
        (" " * (cellWidth + 1))
          .+(
            (0 until cols)
              .map(x => if x < 10 then x.toString + " " * cellWidth else x.toString + " " * (cellWidth - 1))
              .mkString
          )
      )

  def firstBar(cellWidth: Int = 3, row: Int = rows): String =
    if (cellWidth % 2 == 0) {
      "┌" + (("─" * (cellWidth - 1)) + "┬") * (row - 1) + ("─" * (cellWidth - 1)) + "┐" + eol
    } else {
      "┌" + (("─" * cellWidth) + "┬") * (row - 1) + ("─" * cellWidth) + "┐" + eol
    }

  def bar(cellWidth: Int = 3, row: Int = rows): String =
    if (cellWidth % 2 == 0) {
      "├" + (("─" * (cellWidth - 1)) + "┼") * (row - 1) + ("─" * (cellWidth - 1)) + "┤" + eol
    } else {
      "├" + (("─" * cellWidth) + "┼") * (row - 1) + ("─" * cellWidth) + "┤" + eol
    }

  def lastBar(cellWidth: Int = 3, row: Int = rows): String =
    if (cellWidth % 2 == 0) {
      "└" + (("─" * (cellWidth - 1)) + "┴") * (row - 1) + ("─" * (cellWidth - 1)) + "┘" + eol
    } else {
      "└" + (("─" * cellWidth) + "┴") * (row - 1) + ("─" * cellWidth) + "┘" + eol
    }

  def cells(row: Int, cellWidth: Int = 3): String =
    matrix
      .row(row)
      .map(_._1)
      .map(" " * ((cellWidth - 1) / 2) + _ + " " * ((cellWidth - 1) / 2))
      .mkString("│", "│", "│")

  def setBombs(bombNumber: Int): Field =
    setBombsR(bombNumber, this)

  def revealValue(x: Int, y: Int): Field =
    RevealStrategy.strategy(x, y, this)

  def setFlag(x: Int, y: Int): Field =
    val resultField = this
    if detectBombs().size == detectFlags().size then
      if getCell(x, y)._1 == Stone.Flag then ReplaceStrategy.strategy(true, this, x, y, Stone.NotTracked)
      else resultField
    else if getCell(x, y)._1 == Stone.Flag then ReplaceStrategy.strategy(true, this, x, y, Stone.NotTracked)
    else if getCell(x, y)._1 == Stone.NotTracked then ReplaceStrategy.strategy(true, this, x, y, Stone.Flag)
    else resultField

  def detectFlags(): Map[Coordinates, Boolean] =
    var flagMap: Map[Coordinates, Boolean] = Map.empty[Coordinates, Boolean]
    (0 until this.rows).map(i =>
      (0 until this.cols).map(j =>
        if this.getCell(i, j)._1 == Stone.Flag then flagMap = flagMap + (new Coordinates(i, j) -> true)
      )
    )
    flagMap

  def calculateBombAmount(): Int =
    Math.round((this.rows * this.cols).floatValue() * 0.164.floatValue())

  def showValues(): Field = {
    val updatedField = putValues()

    val newField = (0 until updatedField.rows).foldLeft(updatedField) { (field, i) =>
      (0 until updatedField.cols).foldLeft(field) { (fld, j) =>
        val updated = fld.getCell(i, j)._3 match {
          case 1 => ReplaceStrategy.strategy(false, fld, i, j, Stone.One)
          case 2 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Two)
          case 3 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Three)
          case 4 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Four)
          case 5 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Five)
          case 6 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Six)
          case 7 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Seven)
          case 8 => ReplaceStrategy.strategy(false, fld, i, j, Stone.Eight)
          case _ => fld
        }
        updated
      }
    }
    newField
  }

  def getCell(x: Int, y: Int): (Stone, Stone, Int) = matrix.cell(x, y)

  def putValues(): Field = {
    val updatedField = this

    def incrementCell(field: Field, row: Int, col: Int): Field = {
      val cell = field.getCell(row, col)
      val updatedValue = if (cell._2 != Stone.Bomb) cell._3 + 1 else cell._3
      field.setCell(row, col, (cell._1, cell._2, updatedValue))
    }

    val newField = updatedField.detectBombs().foldLeft(updatedField) { (field, bomb) =>
      val (x, y) = (bomb._1.x, bomb._1.y)
      val updatedCells = for {
        i <- -1 to 1
        j <- -1 to 1
        m = x + i
        n = y + j
        if m >= 0 && m < field.rows && n >= 0 && n < field.cols
      } yield (m, n)
      updatedCells.foldLeft(field) { (fld, cellCoords) =>
        val (row, col) = cellCoords
        incrementCell(fld, row, col)
      }
    }
    newField
  }

  def detectBombs(): Map[Coordinates, Boolean] =
    var bombMap: Map[Coordinates, Boolean] = Map.empty[Coordinates, Boolean]
    (0 until this.rows).map(i =>
      (0 until this.cols).map(j =>
        if this.getCell(i, j)._2 == Stone.Bomb then bombMap = bombMap + (new Coordinates(i, j) -> true)
      )
    )
    bombMap

  def setCell(x: Int, y: Int, cell: (Stone, Stone, Int)): Field =
    val fieldd = copy(matrix.replaceCell(x, y, (cell._1, cell._2, cell._3)))
    fieldd

  def matrixx(): Matrix[Stone, Stone, Int] = this.matrix

  def toStone(string: String): Stone =
    if string.equals(Stone.Bomb.toString) then Stone.Bomb
    else if string.equals(Stone.EmptyTracked.toString) then Stone.EmptyTracked
    else if string.equals(Stone.NotTracked.toString) then Stone.NotTracked
    else if string.equals(Stone.Flag.toString) then Stone.Flag
    else if string.equals(Stone.One.toString) then Stone.One
    else if string.equals(Stone.Two.toString) then Stone.Two
    else if string.equals(Stone.Three.toString) then Stone.Three
    else if string.equals(Stone.Four.toString) then Stone.Four
    else if string.equals(Stone.Five.toString) then Stone.Five
    else if string.equals(Stone.Six.toString) then Stone.Six
    else if string.equals(Stone.Seven.toString) then Stone.Seven
    else Stone.Eight

  def flagsLeft(): Int =
    val help = detectBombs().size - detectFlags().size
    help

  override def toJson: JsObject =
    Json.obj(
      "field" -> Json.obj(
        "sizeRow" -> rows,
        "sizeCol" -> cols,
        "cells" -> Json.toJson(
          for {
            row <- 0 until rows
            col <- 0 until cols
          } yield {
            Json.obj(
              "row" -> row,
              "col" -> col,
              "cell" -> Json.obj(
                "first" -> Json.toJson(getCell(row, col)._1.toString),
                "second" -> Json.toJson(getCell(row, col)._2.toString),
                "third" -> Json.toJson(getCell(row, col)._3)
              )
            )
          }
        )
      )
    )

  def jsonToField(jsonString: String): FieldInterface =
    var field: FieldInterface = null
    val json: JsValue = Json.parse(jsonString)
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
    for (index <- 0 until sizeRow * sizeCol)
      val row = (json \\ "row")(index).as[Int]
      val col = (json \\ "col")(index).as[Int]
      val first = field.toStone((json \\ "first")(index).as[String])
      val second = field.toStone((json \\ "second")(index).as[String])
      val third = (json \\ "third")(index).as[Int]
      field = field.setCell(row, col, (first, second, third))
    field
  
  @tailrec
  private def setBombsR(bombNumber: Int, field: Field, count: Int = 0): Field =
    val row = r.nextInt(field.rows)
    val col = r.nextInt(field.cols)
    if count == bombNumber then field
    else if field.matrix.row(row)(col)._2.equals(Stone.Bomb) then setBombsR(bombNumber, field, count)
    else
      val resField = ReplaceStrategy.strategy(false, field, row, col, Stone.Bomb)
      val countR = count + 1
      setBombsR(bombNumber, resField, countR)
