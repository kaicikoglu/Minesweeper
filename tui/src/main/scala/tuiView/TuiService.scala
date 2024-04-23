package tuiView

import com.google.inject.Guice
import controllerComponent.ControllerInterface
import controllerComponent.controllerBaseImpl.MinesweeperModuleEasy

object TuiService {
  @main def startTui(): Unit = {
    println(
      "Insert 1 for easy, 2 for medium or 3 for hard: \n" +
        "To reveal a Field Cell type in the cooridinates as for example: 00, \n" +
        "if you want to place a Flag in this Field write a f behind the cooridnates \n" +
        "Press q to exit, u to undo and r to redo your move"
    )
    val injector = Guice.createInjector(new MinesweeperModuleEasy)
    val controller = injector.getInstance(classOf[ControllerInterface])
    val tui = TUI(controller).run()
  }
}
