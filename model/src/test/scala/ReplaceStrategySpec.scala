import FieldComponent.*
import FieldComponent.FieldBaseImpl.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class ReplaceStrategySpec extends AnyWordSpec {
  "When ReplaceStrategy Pattern is used" should {
    var field = new Field(3, 3)
    field = ReplaceStrategy.strategy(true, field, 0, 0, Stone.Bomb)
    field = ReplaceStrategy.strategy(false, field, 0, 0, Stone.NotTracked)
    "replace the cell with the given stone, if true as parameter, then first tuple place else second" in {
      field.matrix.rows(0)(0)._1 should be(Stone.Bomb)
      field.matrix.rows(0)(0)._2 should be(Stone.NotTracked)
    }
  }
}
