package de.htwg.se.minesweeper.model

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class FieldSpec extends AnyWordSpec {
  "A Minesweeper Field" when {
    "created with default Stone" should {
      val field1 = new Field(1, 1)
      val field2 = new Field(1, 2)
      val field3 = new Field(2, 3)
      val field = new Field(3, 3)
      val eol = field1.eol
      "have a firstBar of form '┌───┬───┬───┐'" in {
        field.firstBar() should be("┌───┬───┬───┐" + eol)
      }
      "have a scalable firstBar with odd cellWidth and even cellWidth parameter" in {
        field2.firstBar(2, 1) should be("┌─┐" + eol)
        field2.firstBar(4, 1) should be("┌───┐" + eol)
        field2.firstBar(6, 3) should be("┌─────┬─────┬─────┐" + eol)
      }
      "have a scalable firstBar with odd cellWidth and odd cellWidth parameter" in {
        field1.firstBar(1, 1) should be("┌─┐" + eol)
        field1.firstBar(3, 1) should be("┌───┐" + eol)
        field1.firstBar(5, 3) should be("┌─────┬─────┬─────┐" + eol)
      }
      "have a bar of form '├───┼───┼───┤'" in {
        field.bar() should be("├───┼───┼───┤" + eol)
      }
      "have a scalable bar with odd cellWidth and even cellWidth parameter" in {
        field2.bar(2, 1) should be("├─┤" + eol)
        field2.bar(4, 1) should be("├───┤" + eol)
        field2.bar(6, 3) should be("├─────┼─────┼─────┤" + eol)
      }
      "have a scalable bar with odd cellWidth and odd cellWidth parameter" in {
        field1.bar(1, 1) should be("├─┤" + eol)
        field1.bar(3, 1) should be("├───┤" + eol)
        field1.bar(5, 3) should be("├─────┼─────┼─────┤" + eol)
      }
      "have a lastBar of form '└───┴───┴───┘'" in {
        field.lastBar() should be("└───┴───┴───┘" + eol)
      }
      "have a scalable lastBar with odd cellWidth and even cellWidth parameter" in {
        field2.lastBar(2, 1) should be("└─┘" + eol)
        field2.lastBar(4, 1) should be("└───┘" + eol)
        field2.lastBar(6, 3) should be("└─────┴─────┴─────┘" + eol)
      }
      "have a scalable lastBar with odd cellWidth and odd cellWidth parameter" in {
        field1.lastBar(1, 1) should be("└─┘" + eol)
        field1.lastBar(3, 1) should be("└───┘" + eol)
        field1.lastBar(5, 3) should be("└─────┴─────┴─────┘" + eol)
      }
      "have cells as String of form '│ \u25A0 │ \u25A0 │ \u25A0 │'" in {
        field.cells(0) should be("│ \u25A0 │ \u25A0 │ \u25A0 │" + eol)
      }
      "have scalable cells" in {
        field1.cells(0, 1) should be("│\u25A0│" + eol)
        field2.cells(0, 1) should be("│\u25A0│\u25A0│" + eol)
        field.cells(0, 3) should be("│ \u25A0 │ \u25A0 │ \u25A0 │" + eol)
      }
      "have a matchField in the form " +
        "┌─┐" +
        "\u25A0" +
        "└─┘" in {
          field1.matchfield(1) should be("┌─┐" + eol + "│\u25A0│" + eol + "└─┘" + eol)
          field3.matchfield(3) should be(
            "┌───┬───┬───┐" + eol + "│ \u25A0 │ \u25A0 │ \u25A0 │" + eol + "├───┼───┼───┤" +
              eol + "│ \u25A0 │ \u25A0 │ \u25A0 │" + eol + "└───┴───┴───┘" + eol
          )
        }
      "have a matchfield with default parameters" in {
        field.matchfield() should be(
          "┌───┬───┬───┐" + eol + "│ ■ │ ■ │ ■ │" + eol + "├───┼───┼───┤" + eol + "│ ■ │ ■ │ ■ │" +
            eol + "├───┼───┼───┤" + eol + "│ ■ │ ■ │ ■ │" + eol + "└───┴───┴───┘" + eol
        )
      }
      "have a toString function" in {
        field.toString should be(
          "┌───┬───┬───┐" + eol + "│ ■ │ ■ │ ■ │" + eol + "├───┼───┼───┤" + eol + "│ ■ │ ■ │ ■ │" +
            eol + "├───┼───┼───┤" + eol + "│ ■ │ ■ │ ■ │" + eol + "└───┴───┴───┘" + eol
        )
      }
      "put function used" should {
        val field = new Field(2, 2, (Stone.NotTracked, Stone.NotTracked))
        val eol = field.eol
        val field2 = field.put(Stone.Flag, 0, 0)
        "have the second element of the tuple changed" in {
          field2.matrix.row(0)(0)._2.toString should be("\u2691")
        }
      }
      "revealValue function used" should {
        val field = new Field(2, 2)
        val field1 = field.revealValue(0, 0)
        "have the tuple elements swap positions" in {
          field.matrix.row(0)(0)._1 should be(Stone.NotTracked)
          field1.matrix.row(0)(0)._1 should be(Stone.EmptyTracked)
        }
        "have the tuple elements not to swap positions, if its value already revealed" in {
          val field2 = field1.revealValue(0, 0)
          field2.matrix.row(0)(0)._1 should be(Stone.EmptyTracked)
        }
      }
      "setBombs function used" should {
        val field = new Field(2, 2)
        val field1 = field.setBombs(2)
        "have the same number of bombs in the field as given as parameter to the function " in {
          var count: Int = 0
          for (i <- (0 until field1.rows))
            for (j <- (0 until field1.cols))
              if (field1.matrix.row(i)(j)._2 == Stone.Bomb)
                count = count + 1
          count should be(2)
        }
      }
    }
  }
}
