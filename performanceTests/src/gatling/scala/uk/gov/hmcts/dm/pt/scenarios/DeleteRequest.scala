package uk.gov.hmcts.dm.pt.scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object DeleteRequest {
  //    val deleteRequest = http(Environments.dmApiGw)
  //      .delete("/documents/2605")
  //      .check(status is 204)
  //
  //    val deleteRequestScenario = scenario("Delete Request Scenario")
  //      .exec(deleteRequest)

  val deleteRequestScenario: ScenarioBuilder = scenario("Delete Request Scenario")
    // .exec(getRequest)
    .pause(1)
    //    .repeat(feeder.records.length){
    //      feed(feeder)
    .exec(http("Delete the record")
    .delete("/api/files/${id}")
    .check(status is 204))  //}
}
