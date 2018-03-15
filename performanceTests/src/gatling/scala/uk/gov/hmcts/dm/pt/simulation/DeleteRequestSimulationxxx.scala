//package uk.gov.hmcts.dm.pt.simulation
//
//import io.gatling.core.Predef._
//import io.gatling.http.Predef.http
//import io.gatling.http.protocol.HttpProtocolBuilder
//import uk.gov.hmcts.dm.pt.scenarios.DeleteRequest
//import uk.gov.hmcts.dm.pt.util.{Environments, Headers}
//
//
//class DeleteRequestSimulation extends Simulation {
//
//  val httpConf: HttpProtocolBuilder = http.baseURL(Environments.dmApiGw).headers(Headers.commonHeader)
//
//  val testScenarioForDeleteRecords = List(DeleteRequest.deleteRequestScenario.inject(atOnceUsers(1)))
//
//
////        setUp(testScenarioForDeleteRecords)
////        .protocols(httpConf)
////        .maxDuration(10 minutes)
////          .assertions(
////            global.responseTime.max.lessThan(DevEnvironment.maxResponseTime.toInt),
////            global.successfulRequests.percent.greaterThan(99))
//}
