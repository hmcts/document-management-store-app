//package uk.gov.hmcts.dm.pt.simulation
//
//import io.gatling.core.Predef._
//import io.gatling.http.Predef._
//import io.gatling.http.protocol.HttpProtocolBuilder
//import uk.gov.hmcts.dm.pt.scenarios.PutRequest
//import uk.gov.hmcts.dm.pt.util.Environments
//
//class PutRequestSimulation extends Simulation{
//
//  //  ScalaJdbcConnectSelect.fill_storedFile()
//
//  val httpConf: HttpProtocolBuilder = http.baseURL(Environments.dmApiGw)//.headers(Headers.commonHeader)
//
//  val testScenarioForPutRecords = List(PutRequest.putRequest2.inject(atOnceUsers(1)))
//
//
//  ////  setUp(
//  ////    PostRequest.postRequestScenario.inject(
//  ////      atOnceUsers(100)
//  //////      rampUsers(1000) over (10 second)
//  ////    ).protocols(httpConf)
//  ////  )
//  //
//  //
//  //  val testScenarios = List(
//  ////      EvidenceSharing.scn3.inject(
//  //
//  ////        constantUsersPerSec(0.08) during (200 second)
//  ////        constantUsersPerSec(0.9) during (20 second)
//  ////        atOnceUsers(1)
//  ////      rampUsers(20) over (200 seconds)
//  ////      constantUsersPerSec(50) during (60 seconds)
//  //      PostRequest.postRequestScenario.inject(atOnceUsers(1)
//  ////        GetRequest.getRequestScenario.inject(atOnceUsers(1)
//  ////        rampUsers(100) over (2 minutes)
//  ////        constantUsersPerSec(50) during (60 seconds)
//  ////        constantUsersPerSec(50) during (120 seconds) randomized
//  ////        rampUsersPerSec(100) to 200 during (2 minutes),
//  ////        rampUsersPerSec(100) to 300 during (3 minutes),
//  ////        splitUsers(500) into (rampUsers(20) over(10 seconds)) separatedBy(10 seconds)
//  ////        )
//  ////      PutRequest.putRequestScenario.inject(atOnceUsers(1)
//  ////      PostRequest.postRequestScenario.inject(
//  ////      DeleteRequest.deleteRequestScenario.inject(atOnceUsers(1)
//  ////      GetRequest.getRequestScenario.inject(
//  ////      GetAllFeesRegister.getAllCategories.inject(
//  ////        atOnceUsers(1)
//  ////        rampUsers(160) over(1 seconds)
//  //        //constantUsersPerSec(800) during(15 seconds)
//  //        //rampUsersPerSec(1) to 100 during(60 seconds) // 6
//  //        //rampUsersPerSec(10) to 20 during(10 minutes) randomized, // 7
//  //        //splitUsers(1000) into(rampUsers(10) over(10 seconds)) separatedBy(10 seconds), // 8
//  //        //splitUsers(1000) into(rampUsers(10) over(10 seconds)) separatedBy atOnceUsers(30), // 9
//  //        //heavisideUsers(1000) over(20 seconds) // 10
//  //      )
//  //
//  ////      GetFeesRegisterByCategoryId.getFeesRegisterByCategoriesId.inject(
//  ////        atOnceUsers(1),
//  ////        rampUsersPerSec(1) to 100 during(300 seconds)
//  ////      ),
//  ////      GetAllFeesRegisterCategories.getAllFeesRegisterCategories.inject(
//  ////        atOnceUsers(1),
//  ////        rampUsersPerSec(1) to 100 during(300 seconds)
//  ////      ),
//  ////      GetAllFlatFeesForGivenCategory.getAllFlatFeesForGivenCategory.inject(
//  ////        atOnceUsers(1),
//  ////        rampUsersPerSec(1) to 100 during(300 seconds)
//  ////      ),
//  ////      //GetAppropriateFlatFeesForGivenFeeId.getAppropriateFlatFeesForGivenFeeId.inject(
//  ////      //atOnceUsers(1),
//  ////      //rampUsersPerSec(1) to 100 during(300 seconds)
//  ////
//  ////      //),
//  ////      GetAppropriateFeesAmountForGivenClaim.getAppropriateFeesAmountForGivenClaim.inject(
//  ////        atOnceUsers(1),rampUsersPerSec(1) to 100 during(300 seconds)
//  ////      ),
//  ////      GetAppropriateFlatFeesForGivenFeeId.getAppropriateFlatFeesForGivenFeeId.inject(atOnceUsers(DevEnvironment.users.toInt))
//  ////        .throttle(reachRps(600) in (20 seconds), holdFor(60 seconds))
//  //    )
//  //
//  //
//  //    setUp(testScenarioForPostRecords)
//  //      setUp(testScenarioForPutRecords)
//  //      .protocols(httpConf)
//  //      .maxDuration(10 minutes)
//  //        .throttle(
//  //        reachRps(100) in (10 seconds),
//  //        holdFor(1 minute),
//  //        jumpToRps(50),
//  //        holdFor(6 minutes)
//  //      )
//  //      .assertions(
//  //        global.responseTime.max.lessThan(DevEnvironment.maxResponseTime.toInt)
//  //      )
//}
