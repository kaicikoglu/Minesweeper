import FieldComponent.FieldBaseImpl.DifficultyFactory
import FieldComponent.FieldInterface
import FileIOComponent.FileIOInterface
import FileIOComponent.fileIoJsonImpl.FileIOJson
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import api.PersistenceApi

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object PersistenceServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("mySystem")
    implicit val executionContext: ExecutionContext = system.dispatcher
    val host = "localhost"
    val port = 8081

    var gameField: FieldInterface = DifficultyFactory("1").run
    gameField = gameField.setBombs(gameField.calculateBombAmount())
    gameField = gameField.showValues()
    val fileIO: FileIOInterface = new FileIOJson

    val fieldApi = new PersistenceApi(gameField, fileIO)
    val routes: Route = fieldApi.routes

    //Start the server
    val bindingFuture = Http().newServerAt(host, port).bind(routes)
    println(s"Server online at http://$host:$port/")
    while (true) {
    }

/*
    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

 */
  }
}
