package guiView

import com.google.inject.Guice
import controllerComponent.ControllerInterface
import controllerComponent.controllerBaseImpl.MinesweeperModuleEasy

object GuiService {
  @main def startGui(): Unit = {
    val injector = Guice.createInjector(new MinesweeperModuleEasy)
    val controller = injector.getInstance(classOf[ControllerInterface])
    val swing = new SwingGui(controller)
  }
}
