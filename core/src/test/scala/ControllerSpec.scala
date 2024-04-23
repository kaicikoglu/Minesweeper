import controllerComponent.*
import controllerComponent.controllerBaseImpl.*
import FieldComponent.*
import FieldComponent.FieldBaseImpl.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class ControllerSpec extends AnyWordSpec {

  "The controller" when {
    val field = new Field(3, 3)
    val controller = Controller(field)
    val eol = field.eol
    "revealValue-function is called" should {
      "have the tuple elements swap positions" in {
        val move = new Coordinates(0, 0)
        controller.revealValue(move).getCell(move.x, move.y)._1 should not be Stone.NotTracked
        controller.revealValue(move) should be(controller.revealValue(move))
      }
    }
    "nostep-function is called" should {
      "not change the field" in {
        controller.noStep(new Coordinates(0, 0)).toString should be(field.toString)
      }
    }
    "undo-function is called" should {
      "undo the previous move" in {
        controller.undo.getCell(0, 0)._1 should be(Stone.NotTracked)
      }
    }
    "redo-function is called" should {
      "redo the previous undo" in {
        controller.redo.getCell(0, 0)._1 should be(Stone.NotTracked)
      }
    }
    "toString-function is called" should {
      "print the field as a string" in {
        controller.toString should be(field.toString)
      }
    }
    "setFlag-function is called" should {
      "set a flag to the coordinates" in {
        controller.setFlag(new Coordinates(0, 0)).cells(0,0).charAt(1).toString should be(Stone.Flag.toString)
      }
    }
    "calculateBombAmount is called" should {
      "calculate the bomb amount" in {
        controller.calculateBombAmount() should be(1)
      }
    }
    "createFieldWithBombs is called" should {
      val helpField = controller.setBombs(controller.calculateBombAmount())
      "have as many bombs in its field as calculated" in {
        var count = 0
        helpField.detectBombs().foreach(x => if x._2 then count = count + 1)
        count should be(1)
      }
    }
    "flagsLeft is called" should {
      var field = new Field(3, 3)
      field = field.setBombs(1)
      field = field.setFlag(0, 0)
      val controller = Controller(field)
      "say how many flags are left to set" in {
        controller.flagsLeft() should be(0)
      }
    }
    "save is called" should {
      val field = new Field(8, 8)
      val controller = Controller(field)
      "save the field" in {
        controller.save.rows should be(8)
      }
    }
    "load is called" should {
      val field = new Field(8, 8)
      val controller = Controller(field)
      "load the field" in {
        controller.load.getCell(0, 0)._1.toString should be(Stone.NotTracked.toString)
      }
    }
    "createNewField is called" should {
      val field = new Field(8, 8)
      val controller = Controller(field)
      "create a new field of the difficulty given as parameter" in {
        controller.createNewField("1").rows should be(8)
        controller.createNewField("2").rows should be(16)
        controller.createNewField("3").rows should be(24)
      }
    }
  }
}
