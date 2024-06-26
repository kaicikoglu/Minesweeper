import FieldComponent.*
import FieldComponent.FieldBaseImpl.*
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
        field.cells(0) should be("│ \u25A0 │ \u25A0 │ \u25A0 │")
      }
      "have scalable cells" in {
        field1.cells(0, 1) should be("│\u25A0│")
        field2.cells(0, 1) should be("│\u25A0│\u25A0│")
        field.cells(0) should be("│ \u25A0 │ \u25A0 │ \u25A0 │")
      }
      "have a matchField in the form " +
        "┌─┐" +
        "\u25A0" +
        "└─┘" in {
        field1.matchfield(1) should be("  0 " + eol + "┌─┐" + eol + "0 │\u25A0│ 0" + eol + "└─┘" + eol + "  0 ")
      }
      "have a matchfield with default parameters" in {
        field1.matchfield() should be(
          "    0   " + eol + "┌───┐" + eol + "0 │ ■ │ 0" + eol + "└───┘" + eol + "    0   "
        )
      }
      "have a toString function" in {
        field1.toString should be(
          "    0   " + eol + "┌───┐" + eol + "0 │ ■ │ 0" + eol + "└───┘" + eol + "    0   "
        )
      }
      "revealValue function used" should {
        val field = new Field(2, 2)
        val field1 = field.revealValue(0, 0)
        "have the tuple elements swap positions" in {
          field.matrix.row(0)(0)._1 should be(Stone.NotTracked)
          field1.matrix.row(0)(0)._1 should be(Stone.EmptyTracked)
        }
      }
      "setBombs function used" should {
        val field = new Field(2, 2)
        val field1 = field.setBombs(2)
        "have the same number of bombs in the field as given as parameter to the function " in {
          var count: Int = 0
          for (i <- 0 until field1.rows)
            for (j <- 0 until field1.cols)
              if (field1.matrix.row(i)(j)._2 == Stone.Bomb)
                count = count + 1
          count should be(2)
        }
      }
      "getCell function used" should {
        val field = new Field(2, 2)
        "get the values of the Field" in {
          field.getCell(0, 0) should be(
            field.matrix.cell(0, 0)._1,
            field.matrix.cell(0, 0)._2,
            field.matrix.cell(0, 0)._3
          )
        }
      }
      "setFlag function used" should {
        val field = new Field(2, 2)
        "set a Flag if the fields first value is not tracked and if there are bombs in the field" in {
          field.setBombs(1).setFlag(0, 0).getCell(0, 0)._1 should be(Stone.Flag)
        }
        "not set a Flag if there are as many flags as bombs in the field" in {
          field.setFlag(0, 1).getCell(0, 0)._1 should not be Stone.Flag
        }
        "remove the Flag if the fields first value is a Flag" in {
          field.setFlag(0, 0).setFlag(0, 0).getCell(0, 0)._1 should be(Stone.NotTracked)
        }
        "dont do anything if the fields value is already revealed" in {
          field.revealValue(0, 0).setFlag(0, 0).getCell(0, 0)._1 should not be Stone.Flag
        }
      }
      "detectBombs function used" should {
        val field = new Field(2, 2)
        "determine the location of the bombs in the field" in {
          field.setBombs(2).detectBombs().size should be(2)
        }
      }
      "detectFlags function used" should {
        val field = new Field(2, 2)
        "determine the number of flags placed in the field" in {
          field.setBombs(1).setFlag(0, 0).detectFlags().size should be(1)
        }
        "be less flags on the field as bombs" in {
          field.setBombs(2).setFlag(0, 1).detectFlags().size should be(1)
        }
      }
      "calculateBombAmount used" should {
        val field = new Field(8, 8)
        "bombs make 16.14% of the field" in {
          field.calculateBombAmount() should be(10)
        }
        "showValues used" should {
          val field = new Field(2, 2)
          "show the Value of the cell" in {
            field.putValues().showValues().getCell(0, 0)._2 should be(Stone.EmptyTracked)
          }
        }
        "putValues used" should {
          var field = new Field(1, 2)
          "Increase the value of the cell" in {
            field = field.setBombs(1)
            field = field.putValues()
            if field.getCell(0, 0)._2.equals(Stone.Bomb) then field.getCell(0, 1)._3 should be(1)
            else field.getCell(0, 0)._3 should be(1)
          }
        }
        "setCell used" should {
          var field = new Field(1, 2)
          "Replace the Cell" in {
            field = field.setCell(0, 0, (Stone.Flag, Stone.Bomb, 1))
            field.getCell(0, 0)._1.toString should be(Stone.Flag.toString)
          }
        }
        "matrixx used" should {
          val field = new Field(1, 2)
          "Return the matrix" in {
            field.matrixx() should be(field.matrix)
          }
        }
        "toStone used" should {
          val field = new Field(1, 2)
          "transform string into Stone" in {
            field.toStone("1") should be(Stone.One)
            field.toStone("2") should be(Stone.Two)
            field.toStone("3") should be(Stone.Three)
            field.toStone("4") should be(Stone.Four)
            field.toStone("5") should be(Stone.Five)
            field.toStone("6") should be(Stone.Six)
            field.toStone("7") should be(Stone.Seven)
            field.toStone("8") should be(Stone.Eight)
            field.toStone("\u25A0") should be(Stone.NotTracked)
            field.toStone("\u25A1") should be(Stone.EmptyTracked)
            field.toStone("✴") should be(Stone.Bomb)
            field.toStone("\u2691") should be(Stone.Flag)
          }
        }
      }
    }
  }
}
