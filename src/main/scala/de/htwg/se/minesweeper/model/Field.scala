package de.htwg.se.minesweeper.model

import scala.util.Random as r

case class Field(matrix: Matrix[Stone, Stone]):
  def this(rows: Int = 3, cols: Int = 3, filling: (Stone, Stone) = (Stone.NotTracked, Stone.EmptyTracked)) =
    this(new Matrix[Stone, Stone](rows, cols, filling))

  val cols: Int = matrix.colNum
  val rows: Int = matrix.rowNum
  val eol: String = sys.props("line.separator")

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
      .mkString("│", "│", "│") + eol

  def matchfield(cellWidth: Int = 3): String =
    (0 until rows)
      .map(cells(_, cellWidth))
      .mkString(firstBar(cellWidth, cols), bar(cellWidth, cols), lastBar(cellWidth, cols))

  override def toString: String = matchfield()

  def put(stone: Stone, x: Int, y: Int): Field = copy(matrix.replaceCell(x, y, (this.matrix.row(x)(y)._1, stone)))

  def getCell(x: Int, y: Int): (Stone, Stone) = matrix.cell(x, y)

  def setBombs(bombNumber: Int = 3): Field =
    setBombsR(bombNumber, this)

  def setBombsR(bombNumber: Int, field: Field, count: Int = 0): Field =
    var row = r.nextInt(rows)
    val col = r.nextInt(cols)
    if (count == bombNumber) then field
    else if (field.matrix.row(row)(col)._2.equals(Stone.Bomb)) then setBombsR(bombNumber, field, count)
    else
      val resField = field.put(Stone.Bomb, row, col)
      val countR = count + 1
      setBombsR(bombNumber, resField, countR)

  def revealValue(x: Int, y: Int): Field =
    if (!this.matrix.row(x)(y)._1.equals(Stone.NotTracked)) then this
    else copy(this.matrix.replaceCell(x, y, (this.matrix.row(x)(y)._2, this.matrix.row(x)(y)._1)))

  def setFlag(x: Int, y: Int): Field =
    var resultField = copy(matrix.replaceCell(x, y, (this.matrix.row(x)(y)._1, this.matrix.row(x)(y)._2)))

    if (matrix.cell(x, y)._1 == Stone.Flag) then
      resultField = copy(matrix.replaceCell(x, y, (Stone.NotTracked, this.matrix.row(x)(y)._2)))
      resultField
    else if (matrix.cell(x, y)._1 == Stone.NotTracked) then
      resultField = copy(matrix.replaceCell(x, y, (Stone.Flag, this.matrix.row(x)(y)._2)))
      resultField
    else resultField
