import FieldComponent.FieldBaseImpl.DifficultyFactory
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class DifficultySpec extends AnyWordSpec {
  "When DifficultyStrategy Pattern is used" should {
    val field1 = DifficultyFactory("1")
    "create a field in easy Mode" in {
      field1.run.rows should be(8)
      field1.run.cols should be(8)
    }
    val field2 = DifficultyFactory("2")
    "create a field in medium mode" in {
      field2.run.rows should be(16)
      field2.run.cols should be(16)
    }
    val field3 = DifficultyFactory("3")
    "create a field in hard mode" in {
      field3.run.rows should be(24)
      field3.run.cols should be(24)
    }
  }
}
