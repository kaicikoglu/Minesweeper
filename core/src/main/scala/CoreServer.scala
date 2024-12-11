import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import api.CoreApi
import com.google.inject.Guice
import controllerComponent.ControllerInterface
import controllerComponent.controllerBaseImpl.MinesweeperModuleEasy
import lib.Servers.coreServer

import scala.concurrent.ExecutionContext

object CoreServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("mySystem")
    implicit val executionContext: ExecutionContext = system.dispatcher

    val coreServerParts = coreServer.split(":")
    val host = coreServerParts(0)
    val port = coreServerParts(1).toInt

    val injector = Guice.createInjector(new MinesweeperModuleEasy)
    val controller = injector.getInstance(classOf[ControllerInterface])
    val controllerApi = new CoreApi(controller)
    val routes: Route = controllerApi.routes

    val bindingFuture = Http().newServerAt(host, port).bind(routes)

    println(s"Server online at http://$host:$port/")
    while (true) {
    }
  }
}
