package de.htwg.se.minesweeper.util

import de.htwg.se.minesweeper.model.*
import de.htwg.se.minesweeper.util.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class RevealStrategySpec extends AnyWordSpec {
  "When RevealStrategy is used" should {
    var field = new Field(2, 2)
    "reveal the value and its neighbours if they are not Bombs" in {
      RevealStrategy.strategy(0, 0, field).getCell(0, 0)._1 should be(Stone.EmptyTracked)
    }
    "reveal the whole field if the value behind this cell is a bomb" in {
      var field1 = new Field(2, 2)
      field1 = field1.setBombs(4)
      field1 = RevealStrategy.strategy(0, 0, field1)
      var count = 0
      for (i <- (0 until field1.rows))
        for (j <- (0 until field1.cols))
          if (field1.getCell(i, j)._2.equals(Stone.NotTracked)) then count = count + 1

      count should be(4)
    }
    "reveal the value of the cell" in {
      var field = new Field(2, 2)
      field = field.setBombs(1)
      field = field.showValues(field)
      if (field.getCell(0, 0)._1.equals(Stone.Bomb)) then
        field = RevealStrategy.strategy(0, 1, field)
        field.getCell(0, 1)._1.toString should be("1")
      else
        field = RevealStrategy.strategy(0, 0, field)
        field.getCell(0, 0)._1.toString should be("1")
    }
  }
}