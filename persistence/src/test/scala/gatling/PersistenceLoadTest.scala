package gatling

import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, PopulationBuilder}

import scala.concurrent.duration.*

class PersistenceLoadTest extends CommonSimConfig {

  override val operations: List[ChainBuilder] = List(
    buildOperation(
      "persistence save",
      "POST",
      "/fileIo",
      ElFileBody("game.json")
    ),
    buildOperation(
      "persistence load",
      "GET",
      "/fileIo/load",
      StringBody("")
    )
  )

  override def executeOperations(): Unit = {
    val scn = buildScenario("Scenario 1")
    var scn2 = buildScenario("Scenario 2")
    var scn3 = buildScenario("Scenario 3")

    setUp(
      // load test with only one user requesting a normal amount of requests
      scn.inject(
        rampUsers(10) during 20.seconds
      )
    ).protocols(httpProtocol)
  }

  executeOperations()
}