package DatabaseComponent.Slick.Tables

import slick.jdbc.PostgresProfile.api._

// GridTable to store the grid details
class GridTable(tag: Tag) extends Table[(Int, Int, Int)](tag, "grid") {
  def gridId = column[Int]("gridId", O.PrimaryKey, O.AutoInc)
  def sizeRow = column[Int]("sizeRow")
  def sizeCol = column[Int]("sizeCol")

  def * = (gridId, sizeRow, sizeCol)
}

// FieldTable to store the cell details
class FieldTable(tag: Tag) extends Table[(Int, Int, Int, Int, String)](tag, "field") {
  def fieldId = column[Int]("fieldId", O.PrimaryKey, O.AutoInc)
  def gridId = column[Int]("gridId")
  def r = column[Int]("r")
  def c = column[Int]("c")
  def stone = column[String]("stone")

  def * = (fieldId, gridId, r, c, stone)

  // Foreign key constraint to the grid
  def grid = foreignKey("GRID_FK", gridId, TableQuery[GridTable])(_.gridId)
}
