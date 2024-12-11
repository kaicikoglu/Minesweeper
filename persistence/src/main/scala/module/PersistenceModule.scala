package module

import com.google.inject.AbstractModule
import DatabaseComponent.MongoDB.MongoUserDAO
import DatabaseComponent.Slick.SlickUserDAO
import DatabaseComponent.UserDAO

class PersistenceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[UserDAO]).to(classOf[SlickUserDAO])
  }
}