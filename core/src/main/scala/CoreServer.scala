import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import api.CoreApi
import com.google.inject.Guice
import controllerComponent.ControllerInterface
import controllerComponent.controllerBaseImpl.MinesweeperModuleEasy

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object CoreServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("mySystem")
    implicit val executionContext: ExecutionContext = system.dispatcher
    val port = 8082

    val injector = Guice.createInjector(new MinesweeperModuleEasy)
    val controller = injector.getInstance(classOf[ControllerInterface])
    val controllerApi = new CoreApi(controller)
    val routes: Route = controllerApi.routes

    val bindingFuture = Http().newServerAt("localhost", port).bind(routes)

    println(s"Controller Server online at http://localhost:$port/\nPress Return to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
