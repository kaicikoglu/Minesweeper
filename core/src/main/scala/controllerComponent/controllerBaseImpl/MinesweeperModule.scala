package controllerComponent.controllerBaseImpl

import com.google.inject.AbstractModule
import controllerComponent.*
import controllerComponent.ControllerInterface
import FieldComponent.FieldBaseImpl.DifficultyFactory
import FileIOComponent.FileIOInterface
import FileIOComponent.fileIoJsonImpl.FileIOJson
import FileIOComponent.fileIoXmlImpl.FileIOXml

class MinesweeperModuleEasy extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ControllerInterface]).toInstance(Controller(DifficultyFactory("1").run))
  }
}

class MinesweeperModuleMedium extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ControllerInterface]).toInstance(Controller(DifficultyFactory("2").run))
  }
}

class MinesweeperModuleHard extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ControllerInterface]).toInstance(Controller(DifficultyFactory("3").run))
  }
}

class MinesweeperXML extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[FileIOInterface]).toInstance(new FileIOXml())
  }
}

class MinesweeperJson extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[FileIOInterface]).toInstance(new FileIOJson())
  }
}
