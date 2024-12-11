package gatling

import io.gatling.core.Predef.*
import io.gatling.core.body.Body
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.*


abstract class CommonSimConfig extends Simulation {

  val operations: List[ChainBuilder]

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8081")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")

  val headers: Map[String, String] = Map(
    "Cache-Control" -> "max-age=0",
    "Sec-Fetch-Dest" -> "document",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "none",
    "Sec-Fetch-User" -> "?1",
    "sec-ch-ua" -> """Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-ch-ua-platform" -> "Windows"
  )

  def buildOperation(name: String, request: String, operation: String, body: Body): ChainBuilder = {
    exec(
      http(name)
        .httpRequest(request, operation)
        .body(body)
    )
  }

  def buildScenario(name: String): ScenarioBuilder =
    scenario(name)
      .exec(
        //exec the operations and between each pause a second
        operations.reduce((a, b) => a.pause(1.second).exec(b))
      )

  def executeOperations(): Unit
}