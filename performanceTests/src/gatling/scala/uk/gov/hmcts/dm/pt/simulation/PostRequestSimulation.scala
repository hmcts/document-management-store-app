package uk.gov.hmcts.dm.pt.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef.http
import uk.gov.hmcts.dm.pt.scenarios.PostRequest
import uk.gov.hmcts.dm.pt.util.{Environments, Headers}

import scala.concurrent.duration._
import scala.language.postfixOps

class PostRequestSimulation extends Simulation {

  val httpConf = http.disableWarmUp.baseURL(Environments.dmStore).headers(Headers.commonHeader)

//  val testScenarioFinal = scenario("Post").exec(PostRequest.storeScn, PostRequest.fetchScn)

  val randomPostRequest = scenario("Random Post").exec(PostRequest.storeScn)

  val randomGetRequest = scenario("Random GetRequest").exec(PostRequest.fetchScn)

  val postAndGetInSeq = scenario("Post and Get in sequence").exec(PostRequest.storeInSeq, PostRequest.fetchInSeq)

  val testScenarios = List(postAndGetInSeq.inject(rampUsers(5) over (1 minutes)))

    val randomTestScenarios = List(randomPostRequest.inject(splitUsers(400) into (rampUsers(50) over (30 seconds)) separatedBy atOnceUsers(7)), //atOnceUser(40)//rampUsers(4) over (2 minutes)),splitUsers(40) into (rampUsers(5) over (10 seconds)) separatedBy (10 seconds)
        randomGetRequest.inject( nothingFor(20 seconds), splitUsers(400) into (rampUsers(50) over (30 seconds)) separatedBy atOnceUsers(7)))//nothingFor(5 seconds), atOnceUsers(40)))//rampUsers(4) over (2 minutes))   )

    val prodLikeScenarios = List(randomPostRequest.inject(splitUsers(200) into (rampUsers(2) over (3 seconds)) separatedBy (3 seconds)), //atOnceUser(40)//rampUsers(4) over (2 minutes)),splitUsers(40) into (rampUsers(5) over (10 seconds)) separatedBy (10 seconds)
        randomGetRequest.inject( nothingFor(40 seconds), splitUsers(200) into (rampUsers(3) over (3 seconds)) separatedBy (3 seconds)))

//    val prodLikeScenarios = List(randomPostRequest.inject(splitUsers(200) into (rampUsers(4) over (3 seconds)) separatedBy atOnceUsers(1)), //atOnceUser(40)//rampUsers(4) over (2 minutes)),splitUsers(40) into (rampUsers(5) over (10 seconds)) separatedBy (10 seconds)
//        randomGetRequest.inject( nothingFor(20 seconds), splitUsers(200) into (rampUsers(4) over (2 seconds)) separatedBy atOnceUsers(1)))
//        randomPostRequest.inject(rampUsers(20) over (30 seconds)), //atOnceUser(40)//rampUsers(4) over (2 minutes)),splitUsers(40) into (rampUsers(5) over (10 seconds)) separatedBy (10 seconds)
//        randomGetRequest.inject( nothingFor(10 seconds), rampUsers(20) over (30 seconds)))

    //    val randomTestScenarios = List(randomPostRequest.inject(splitUsers(300) into (rampUsers(32) over (10 seconds)) separatedBy atOnceUsers(7)), //atOnceUser(40)//rampUsers(4) over (2 minutes)),splitUsers(40) into (rampUsers(5) over (10 seconds)) separatedBy (10 seconds)
//        randomGetRequest.inject( nothingFor(2 seconds), splitUsers(300) into (rampUsers(32) over (9 seconds)) separatedBy atOnceUsers(7)))//nothingFor(5 seconds), atOnceUsers(40)))//rampUsers(4) over (2 minutes))   )
    //  val testScenarioForPostRecords = List(PostRequest.postRequestScenario.inject(atOnceUsers(1),rampUsers(38) over (2 minute)))

//  val postRequests2000 = List(PostRequest.postRequestScenario.inject(atOnceUsers(1), rampUsersPerSec(1) to 200 during (1 minute)))

//  setUp(testScenarios)
//    setUp(randomTestScenarios)
    setUp(prodLikeScenarios)
    .protocols(httpConf)
    .maxDuration(10 minutes)
    .assertions(
      global.responseTime.max.lte(Environments.maxResponseTime.toInt),
      global.successfulRequests.percent.gte(99))
}
