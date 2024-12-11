package gatling

import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, PopulationBuilder}

import scala.concurrent.duration.*

class PersistenceSpikeTest extends CommonSimConfig {

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
      scn.inject(
        // spike users
        rampUsers(2) during 20.second,
        atOnceUsers(300),
        rampUsers(2) during 10.second
      )
    ).protocols(httpProtocol)
  }

  executeOperations()
}