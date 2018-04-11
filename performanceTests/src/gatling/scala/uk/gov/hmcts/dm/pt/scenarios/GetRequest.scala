package uk.gov.hmcts.dm.pt.scenarios

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmcts.dm.pt.util.idamTokenGenerator
import uk.gov.hmcts.dm.pt.util.Environments

object GetRequest {

  val feeder: RecordSeqFeederBuilder[String] = csv("listofcases");

  val serviceToken: String = idamTokenGenerator.generateS2SToken()

  val getRequest: HttpRequestBuilder = http(Environments.dmStore)
    .get("/documents/${id}")
    .check(status is 200)


  val getRequestScenario: ScenarioBuilder = scenario("Get Request Scenario")
    .pause(1)
    .repeat(feeder.records.length){
      feed(feeder)
        .exec(http("Test ${id}")
          .get("/documents/${id}")
          .check(status is 200))  }


  val getRequestScenarioForSingleRecord: ScenarioBuilder = scenario("Get Request Scenario for single record")
    .repeat(1){
      feed(feeder.random)
        .exec(http("Test ${id}")
          .get(Environments.dmStore+"/api/files/86d4efab-fa41-4681-913f-d7931d94e2a2")
          .header("Authorization", serviceToken).header("user-id", "gatling")
          .check(status is 200, header("$").saveAs("jwtToken")))  }
    .exec{
      session => println("Response ===========>" + session.get("jwtToken"))
      session
    }
}
