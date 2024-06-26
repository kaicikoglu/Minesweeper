package FieldComponent.FieldBaseImpl

object RevealStrategy:
  def strategy(x: Int, y: Int, field: Field): Field = if field.getCell(x, y)._2.equals(Stone.Bomb) then
    strategy1(x, y, field)
  else if field.getCell(x, y)._3 != 0 then strategy2(x, y, field)
  else strategy3(x, y, field)

  private def reveal(x: Int, y: Int, field: Field): Field =
    field.copy(
      field.matrix.replaceCell(x, y, (field.getCell(x, y)._2, field.getCell(x, y)._1, field.getCell(x, y)._3))
    )

  private def strategy1(x: Int, y: Int, field: Field): Field =
    var res: Field = field
    (0 until field.rows).map(i =>
      (0 until field.cols).map(j => if !field.getCell(i, j)._2.equals(Stone.NotTracked) then res = reveal(i, j, res))
    )
    res

  private def strategy2(x: Int, y: Int, field: Field): Field =
    reveal(x, y, field)

  private def strategy3(x: Int, y: Int, field: Field): Field =
    var res = reveal(x, y, field)
    if res.getCell(x, y)._3 == 0 then res = revealNeighbours(x, y, res)
    res

  private def revealNeighbours(x: Int, y: Int, field: Field): Field =
    var res = field
    (-1 until 2).map(i =>
      (-1 until 2).map(j =>
        val m = x + i
        val n = y + j
        if m > -1 && m < res.rows && n > -1 && n < res.cols then
          if !res.getCell(m, n)._2.equals(Stone.Bomb) && res.getCell(m, n)._1.equals(Stone.NotTracked) then
            res = strategy3(m, n, res)
      )
    )
    res
