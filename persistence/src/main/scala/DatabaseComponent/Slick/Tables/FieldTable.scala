package DatabaseComponent.Slick.Tables

import slick.jdbc.PostgresProfile.api._

// GridTable to store the grid details
class GridTable(tag: Tag) extends Table[(Int, Int, Int)](tag, "grid") {
  def gridId = column[Int]("gridId", O.PrimaryKey, O.AutoInc)
  def sizeRow = column[Int]("sizeRow")
  def sizeCol = column[Int]("sizeCol")

  def * = (gridId, sizeRow, sizeCol)
}

// FieldTable to store the cell details, including the first, second, and third elements from the JSON structure
class FieldTable(tag: Tag) extends Table[(Int, Int, Int, Int, String, String, Int)](tag, "field") {
  def fieldId = column[Int]("fieldId", O.PrimaryKey, O.AutoInc)
  def gridId = column[Int]("gridId")
  def r = column[Int]("r")  // row index
  def c = column[Int]("c")  // column index
  def first = column[String]("first")  // corresponds to the "first" element in your JSON
  def second = column[String]("second")  // corresponds to the "second" element in your JSON
  def third = column[Int]("third")  // corresponds to the "third" element in your JSON

  // Foreign key constraint to the grid
  def grid = foreignKey("GRID_FK", gridId, TableQuery[GridTable])(_.gridId)

  def * = (fieldId, gridId, r, c, first, second, third)
}
