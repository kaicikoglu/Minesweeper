package tuiView

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

object TuiService {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem() // ActorSystem provides the default materializer
    implicit val ec: ExecutionContext = system.dispatcher // ExecutionContext comes from the system
    implicit val materializer: Materializer = Materializer(system) // Add implicit Materializer

    println(
      "Insert 1 for easy, 2 for medium or 3 for hard: \n" +
        "To reveal a Field Cell type in the cooridinates as for example: 00, \n" +
        "if you want to place a Flag in this Field write a f behind the cooridnates \n" +
        "Press q to exit, u to undo and r to redo your move"
    )

    val tui = new TUI("http://localhost:8082") // Pass the core service URL
    tui.run()
  }
}
