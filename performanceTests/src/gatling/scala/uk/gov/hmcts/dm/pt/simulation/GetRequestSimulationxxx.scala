//package uk.gov.hmcts.dm.pt.simulation
//
//import io.gatling.core.Predef._
//import io.gatling.http.Predef.http
//import io.gatling.http.protocol.HttpProtocolBuilder
//import uk.gov.hmcts.dm.pt.scenarios.GetRequest
//import uk.gov.hmcts.dm.pt.util.{Environments, Headers}
//
//class GetRequestSimulation extends Simulation {
//
//  //  ScalaJdbcConnectSelect.fill_storedFile()
//  val httpConf: HttpProtocolBuilder = http.baseURL(Environments.dmApiGw).headers(Headers.commonHeader)
//
//  val testScenarioForSingleGetRecord = List(GetRequest.getRequestScenarioForSingleRecord.inject(atOnceUsers(1)
//      //      rampUsers(2000) over (1 minutes),
//      //      constantUsersPerSec(900) during (60 seconds)
//    )
//  )
//
////  setUp(testScenarioForSingleGetRecord)
////    .protocols(httpConf)
////    .maxDuration(10 minutes)
////    .throttle(
////      reachRps(100) in (10 seconds),
////      holdFor(1 minute),
////      jumpToRps(50),
////      holdFor(6 minutes)
////    )
//}
