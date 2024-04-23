import FieldComponent.*
import FieldComponent.FieldBaseImpl.*
import controllerComponent.*
import controllerComponent.controllerBaseImpl.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import tuiView.TUI

class TUISpec extends AnyWordSpec {
  "The view.TUI" should {
    val tui = TUI(Controller(new Field(3, 3, (Stone.NotTracked, Stone.EmptyTracked, 0))))
    "recognize the input 00 as coordinates(0,0)" in {
      tui.parseInput("00") should be(Some(new Coordinates(0, 0)))
    }
    "recognize the input 00f as coorinates(0,0) and extra char to set the flag to the coordinates" in {
      tui.parseInput("00f") should be(Some(Coordinates(0, 0, ' ')))
    }
  }
}
