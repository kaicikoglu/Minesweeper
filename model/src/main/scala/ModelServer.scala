import FieldComponent.FieldBaseImpl.DifficultyFactory
import FieldComponent.FieldInterface
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import api.ModelApi
import lib.Servers.modelServer

import scala.concurrent.ExecutionContext

object ModelServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("mySystem")
    implicit val executionContext: ExecutionContext = system.dispatcher

    val modelServerParts = modelServer.split(":")
    val host = modelServerParts(0)
    val port = modelServerParts(1).toInt

    var gameField: FieldInterface = DifficultyFactory("1").run
    gameField = gameField.setBombs(gameField.calculateBombAmount())
    gameField = gameField.showValues()
    val fieldApi = new ModelApi(gameField)
    val routes: Route = fieldApi.routes

    val bindingFuture = Http().newServerAt(host, port).bind(routes)
    println(s"Server online at http://$host:$port/")
    while (true) {}

    /*
    println(s"Server online at http://$host:$port/\nPress Return to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

     */
  }
}
